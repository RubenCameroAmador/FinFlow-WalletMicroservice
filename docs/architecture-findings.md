# Revisión de arquitectura — hallazgos (creado 2026-07-28, actualizado 2026-07-28)

Notas de una revisión puntual del estado del proyecto (dominio `Wallet` + aplicación + infraestructura), guardadas para retomarlas más adelante.

## Lo que está bien logrado

- Dominio limpio: `Wallet` no importa nada de Spring/JPA.
- `Money` es un value object inmutable con `equals`/`hashCode` bien implementados.
- Dos mappers separados (API y persistencia) evitan que el modelo de dominio se filtre hacia afuera.
- `Wallet.reconstitute(...)` rehidrata desde BD sin relanzar eventos — comportamiento correcto para un aggregate.
- Patrón *event collection* (`addEvent` / `pullEvents()`) implementado correctamente en `Wallet`.

## Resuelto

1. **Cableado de Spring (bloqueaba el arranque).** Los 6 use cases ahora son `@Service`, y se implementó `KafkaDomainEventPublisher` (adaptador real de `DomainEventPublisher`, ver [kafka-integration.md](./kafka-integration.md)), cableado como `@Bean` en `FinflowApiApplication`. Confirmado end-to-end: la app arranca, se pudo crear una wallet y depositar dinero.
2. **Sin manejo global de excepciones.** Se creó `infrastructure/api/exception/` con `ErrorResponse` (DTO) y `WalletExceptionHandler` (`@RestControllerAdvice`), que intercepta `InvalidWalletException` y devuelve `409 Conflict` con un JSON estructurado en vez del 500 genérico. Sigue pendiente la granularidad fina (ver más abajo).
3. **`@GeneratedValue(IDENTITY)` sobre PK UUID.** Se quitó — ahora `WalletEntity.walletId` solo tiene `@Id`. Esto en realidad causaba que la tabla `wallets` nunca se creara (Postgres rechaza `IDENTITY` sobre columnas no numéricas), y por eso salía `relation "wallets" does not exist` al hacer el primer `POST`.
4. **Integración con Kafka funcionando end-to-end.** Publicación de domain events (`WalletCreated`, `MoneyDeposited`, etc.) traducidos a integration events (`infrastructure/messaging/event/`) vía `WalletEventMapper`, publicados al topic `wallet-events` (key = `walletId`) desde `KafkaDomainEventPublisher`. Dos gotchas específicos de Spring Boot 4.1 que costó diagnosticar, documentados aquí para no repetirlos:
   - La autoconfiguración de Kafka (que crea el bean `KafkaTemplate`) vive en un módulo separado, `spring-boot-starter-kafka` — depender solo de `spring-kafka` no alcanza.
   - El `KafkaTemplate` autoconfigurado es `KafkaTemplate<Object, Object>` (no `<String, Object>`) — hay que matchear ese tipo genérico exacto en los puntos de inyección.
   - El `JsonSerializer` clásico de Spring Kafka depende de Jackson 2 (`com.fasterxml.jackson.databind`), pero Spring Boot 4.1 trae Jackson 3 (`tools.jackson.*`) por defecto — hay que usar `JacksonJsonSerializer` en su lugar.
5. **Bug en `withdraw` (saldo insuficiente nunca se detectaba).** `Money.subtract` construía un `Money` con el resultado negativo, y el constructor de `Money` lanzaba `IllegalArgumentException` antes de que `Wallet.withdraw` pudiera comprobar `newBalance.isNegative()` y lanzar su propio `InvalidWalletException("Insufficient balance.")` — esa rama nunca se ejecutaba. Se agregó `Money.isLessThan(Money other)`, y `withdraw` ahora valida `this.balance.isLessThan(amount)` **antes** de restar, evitando construir un `Money` negativo. De paso, esto también cierra el hueco con `WalletExceptionHandler`: antes, un retiro con saldo insuficiente daba 500 (porque `IllegalArgumentException` no está mapeada por el `@RestControllerAdvice`); ahora da `409 Conflict` como corresponde.
6. **Swagger/OpenAPI confirmado funcionando.** `springdoc-openapi-starter-webmvc-ui:2.8.6` arranca sin problemas sobre Spring Boot 4.1 / Spring Framework 7, pese a no tener soporte declarado explícitamente para esa versión.
7. **`WalletId`/`OwnerId` no sobreescribían `equals`/`hashCode`.** Se agregó el mismo patrón que ya tenía `Money` (comparar por el `UUID` interno). Sin esto, dos instancias envolviendo el mismo UUID se consideraban "distintas" (comparación por referencia), lo cual rompía silenciosamente cualquier uso en `Set`/`Map` o comparaciones directas — ver explicación completa en la conversación de esta fecha.
8. **Typo `owerId`**: corregido en `WalletEntity` y `WalletMapper` — ahora `ownerId` en ambos.
9. **Invariante "toda wallet nueva empieza ACTIVE" movido al dominio.** El constructor de `Wallet` ya no acepta `status` — siempre asigna `WalletStatus.ACTIVE` internamente, así que es imposible construir una wallet nueva en otro estado (antes el constructor lo permitía, aunque nadie lo hiciera en la práctica). Se quitó el campo `status` de `CreateWalletCommand` y la línea que lo fabricaba en `WalletApiMapper` (junto al import de `WalletStatus`, ya sin uso ahí). El contrato externo (`CreateWalletRequest`) no cambia — nunca tuvo ese campo.

## Pendiente

1. **`spring-boot-starter-validation` está en el `pom.xml` pero no se usa** en ningún DTO.
2. **Sin tests** más allá del `contextLoads()` por defecto de Spring Boot.
3. **`InvalidWalletException` sigue siendo un único tipo genérico** para todos los errores de negocio (wallet no encontrada, ya congelada, ya activa, saldo insuficiente...). Por eso `WalletExceptionHandler` mapea *todo* a `409 Conflict`, incluso "wallet no encontrada", que semánticamente debería ser `404 Not Found`. Para distinguirlo bien haría falta una jerarquía de excepciones de dominio (ej. `WalletNotFoundException` separada).

**Nota de corrección:** el hallazgo original "`deposit()` no bloquea wallets `FROZEN`" era incorrecto — se revisó `Wallet.java` y `deposit()` sí valida `FROZEN` (líneas 93-97), igual que `withdraw()`. Quedó descartado, no era un bug real.

## Cuándo retomar esto

Ya no hay ningún bloqueante para correr el servicio, ni bugs funcionales conocidos. Lo que queda es deuda de calidad (puntos 1-3 de "Pendiente"). Buen candidato para la próxima sesión: la jerarquía de excepciones de dominio (punto 3), que además mejora el `WalletExceptionHandler` ya existente.
