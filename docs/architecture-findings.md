# Revisión de arquitectura — hallazgos (2026-07-28)

Notas de una revisión puntual del estado del proyecto (dominio `Wallet` + aplicación + infraestructura), guardadas para retomarlas más adelante. No implican que algo esté roto de forma crítica salvo el punto 1, que bloquea arrancar la app.

## Lo que está bien logrado

- Dominio limpio: `Wallet` no importa nada de Spring/JPA.
- `Money` es un value object inmutable con `equals`/`hashCode` bien implementados.
- Dos mappers separados (API y persistencia) evitan que el modelo de dominio se filtre hacia afuera.
- `Wallet.reconstitute(...)` rehidrata desde BD sin relanzar eventos — comportamiento correcto para un aggregate.
- Patrón *event collection* (`addEvent` / `pullEvents()`) implementado correctamente en `Wallet`.

## Pendientes a revisar

1. **La app probablemente no arranca aún**: `WalletController` pide los 6 casos de uso por constructor, pero ninguno es bean de Spring (`@Component`/`@Service`) ni hay `@Bean` para ellos en `FinflowApiApplication`. Tampoco existe ninguna implementación del puerto `DomainEventPublisher` — el puerto está definido (`application/port/DomainEventPublisher.java`) pero sin adaptador. Solo `WalletRepository` está cableado manualmente.
2. **Bug real en `withdraw`**: `Money.subtract` lanza `IllegalArgumentException` si el resultado es negativo, *antes* de que `Wallet.withdraw` llegue a comprobar `newBalance.isNegative()` para lanzar su propio `InvalidWalletException("Insufficient balance.")`. Esa rama de "saldo insuficiente" nunca se ejecuta en la práctica.
3. **`deposit()` no bloquea wallets `FROZEN`**, a diferencia de `withdraw()` que sí lo hace — inconsistencia de invariante (puede ser intencional, decidirlo conscientemente).
4. **`WalletId`/`OwnerId` no sobreescriben `equals`/`hashCode`**, a diferencia de `Money` — inconsistente entre value objects.
5. **Typo**: campo `owerId` en `WalletEntity` y `WalletMapper` (debería ser `ownerId`).
6. **Sin `@ControllerAdvice`**: cualquier `InvalidWalletException` se traduce en un 500 genérico en vez de 404/409/400.
7. **`spring-boot-starter-validation` está en el `pom.xml` pero no se usa** en ningún DTO.
8. **El invariante "toda wallet nueva empieza ACTIVE"** vive en `WalletApiMapper` (infraestructura), no en el dominio — el constructor de `Wallet` acepta cualquier estado inicial.
9. **`@GeneratedValue(IDENTITY)` sobre una PK UUID** que la app siempre pre-asigna (`WalletEntity`) — probablemente vestigial.
10. **Sin tests** más allá del `contextLoads()` por defecto de Spring Boot.

## Cuándo retomar esto

El punto 1 (cableado de Spring) es el único que probablemente impide levantar el servicio; conviene resolverlo antes o junto con la integración de Kafka (ver [kafka-integration.md](./kafka-integration.md)), ya que el `DomainEventPublisher` es justo el puerto que un adaptador de Kafka implementaría.
