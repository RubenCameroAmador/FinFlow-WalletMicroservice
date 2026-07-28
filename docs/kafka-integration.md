# Integración con Kafka — Guía Wallet Service

## Estado Actual

Ya tienes:
- Un broker Kafka local corriendo en Docker (`apache/kafka:4.1.0`, contenedor `kafka`, puerto `9092` expuesto en `0.0.0.0:9092`).
- Domain events ya modelados: `WalletCreated`, `WalletActivated`, `WalletFrozen`, `MoneyDeposited`, `MoneyWithdrawn` (todos implementan `DomainEvent`, con `eventId()` y `occurredOn()`).
- El patrón de recolección de eventos ya funciona: `Wallet.addEvent(...)` los acumula internamente y `Wallet.pullEvents()` los devuelve y limpia la lista.
- El puerto de salida `DomainEventPublisher` ya existe (`application/port/DomainEventPublisher.java`) con un único método `publish(List<DomainEvent> events)`.
- Cada caso de uso (`DepositMoneyUseCase`, `WithdrawMoneyUseCase`, etc.) ya llama a `domainEventPublisher.publish(wallet.pullEvents())` después de guardar. **Este es el punto exacto donde Kafka entra** — hoy esa llamada no tiene ninguna implementación real detrás (ver `docs/architecture-findings.md`, punto 1).

Es decir: **el hueco en el hexágono ya está definido**. Lo que falta es el adaptador de salida que implemente `DomainEventPublisher` usando un producer de Kafka, y (más adelante) un adaptador de entrada que consuma esos eventos.

---

## Teoría: ¿por qué Kafka encaja aquí?

### Domain Events vs. Integration Events

En DDD se distinguen dos conceptos que a veces se confunden:

- **Domain Event**: algo que pasó *dentro* de un aggregate, relevante para el propio bounded context (`MoneyDeposited`). Vive en el dominio, no sabe nada de mensajería.
- **Integration Event**: la versión de ese evento que cruza los límites del servicio hacia otros sistemas/microservicios. Normalmente es una *representación serializada* (JSON/Avro) del domain event, a veces con menos o más campos según lo que el consumidor externo necesita.

**Pregunta para reflexionar:** ¿Debería `MoneyDeposited` (tu domain event) ser exactamente lo que se serializa y se envía a Kafka, o deberías tener una clase/DTO de "evento de integración" separada? Piensa en qué pasa si mañana cambias un campo interno de `MoneyDeposited` que no debería afectar a los consumidores externos.

### Dónde vive Kafka en la arquitectura hexagonal

Kafka **no es parte del dominio ni de la aplicación** — es infraestructura, igual que PostgreSQL. La diferencia es que aquí actúa como **adaptador de salida** (cuando publicas eventos) y potencialmente como **adaptador de entrada** (si este u otro microservicio consume eventos de un topic).

```
┌─────────────────────────┐
│   Use Case              │  ← ya existente, ya llama a domainEventPublisher.publish(...)
└────────────┬─────────────┘
             │ usa el puerto
             ▼
┌─────────────────────────┐
│  DomainEventPublisher    │  ← puerto de salida (application/port), YA EXISTE
│  (interfaz)              │
└────────────┬─────────────┘
             │ implementado por
             ▼
┌─────────────────────────┐
│ KafkaDomainEventPublisher│  ← adaptador de salida, FALTA CREARLO
│ (infrastructure/messaging)│
└────────────┬─────────────┘
             │ usa
             ▼
┌─────────────────────────┐
│   KafkaTemplate /        │
│   Producer                │
└────────────┬─────────────┘
             │ produce a un topic
             ▼
        ┌─────────┐
        │  Kafka  │
        └─────────┘
```

**Punto clave:** el dominio y la aplicación no deberían enterarse de que existe Kafka. Si mañana cambias Kafka por RabbitMQ, solo tocas el adaptador — ni el `Wallet`, ni los use cases, ni el puerto `DomainEventPublisher` deberían cambiar.

### Conceptos de Kafka que necesitas entender antes de escribir código

No los voy a explicar en profundidad aquí (búscalos y entiéndelos por tu cuenta, es parte del aprendizaje), pero identifica qué significan y por qué importan para este caso:

- **Topic**: ¿vas a usar un topic por tipo de evento (`wallet.created`, `wallet.money-deposited`, ...) o un solo topic (`wallet-events`) con todos los eventos mezclados? Cada enfoque tiene trade-offs de orden y consumo.
- **Key del mensaje**: en Kafka, los mensajes con la misma key van a la misma partición, lo que garantiza orden *entre mensajes de la misma key*. ¿Qué deberías usar como key para los eventos de una wallet? (pista: piensa en qué garantiza que los eventos de la misma wallet se procesen en orden).
- **Particiones**: ¿cuántas particiones le pondrías a tu topic? ¿Qué relación tiene con el paralelismo de consumidores?
- **Serialización**: ¿JSON, Avro, Protobuf? Para aprender, JSON es más simple y suficiente. Con Avro/Protobuf normalmente entra en juego un Schema Registry (fuera de alcance por ahora).
- **Producer acks**: ¿qué garantía de entrega necesitas (`acks=0`, `1`, `all`)? Relaciona esto con qué tan crítico es que un evento financiero (`MoneyWithdrawn`) se pierda.
- **Consumer groups** (para más adelante, cuando consumas): cómo Kafka reparte particiones entre instancias de un mismo grupo.

---

## Paso 1: Dependencia de Spring Kafka

### Tarea

Agrega la dependencia de Spring for Apache Kafka a tu `pom.xml`. Busca el artifact correcto (pista: `spring-kafka`, del grupo `org.springframework.kafka`) y la versión compatible con tu Spring Boot `4.1.0`.

**Pregunta:** ¿Spring Boot gestiona la versión de `spring-kafka` automáticamente por el BOM del `spring-boot-starter-parent`, o necesitas fijarla tú explícitamente?

---

## Paso 2: Configuración de conexión

### Tarea

En `src/main/resources/application.properties`, agrega las propiedades necesarias para que Spring sepa cómo conectarse a tu broker local (`localhost:9092`, según el contenedor que ya tienes corriendo).

Necesitarás configurar, como mínimo:
- La dirección del/los broker(s) (`bootstrap-servers`).
- El serializer de la key y del value para el producer.

**Pregunta:** Si vas a serializar tus eventos como JSON, ¿qué serializer de Spring Kafka deberías usar para el value? ¿Y qué tipo de dato usarías como key (String, UUID)?

**Verificación:** antes de escribir código Java, valida que puedes hablar con tu broker desde la terminal. Investiga cómo:
- Listar los topics existentes en tu contenedor Kafka.
- Crear un topic manualmente (o dejar que se autocree, si tu broker lo permite).
- Producir y consumir un mensaje de prueba usando las herramientas de línea de comandos que trae la imagen `apache/kafka`.

Esto te da una base para saber si un problema futuro es de tu código Java o de la conexión/configuración del broker.

---

## Paso 3: Decidir el modelo de eventos de integración

Antes de escribir el adaptador, decide (repasa la sección de teoría arriba):

1. ¿Vas a serializar directamente tus `DomainEvent` (records de `domain/event/`), o vas a crear una capa de traducción (p. ej. `infrastructure/messaging/dto/`) con la forma exacta del mensaje que sale a Kafka?
2. ¿Qué estructura tendrá el mensaje? Como mínimo necesitarás algo como: tipo de evento, `eventId`, `occurredOn`, `walletId`, y el payload específico de cada evento.
3. ¿Un topic único (`wallet-events`) o un topic por tipo de evento?

**Tarea:** Documenta tu decisión (puede ser un comentario breve en el propio código o una nota tuya) antes de seguir. No hay una única respuesta correcta — lo importante es que entiendas el trade-off.

---

## Paso 4: Implementar el adaptador de salida

### Paquete sugerido

```
infrastructure/
└── messaging/
    ├── KafkaDomainEventPublisher.java   ← implementa DomainEventPublisher
    └── (opcional) mapper/ o dto/ para los eventos de integración
```

### `KafkaDomainEventPublisher`

**Qué debe hacer:**
- Implementar la interfaz `application/port/DomainEventPublisher.java` que ya tienes.
- Recibir la `List<DomainEvent>` en `publish(...)`.
- Por cada evento, decidir el topic destino y la key del mensaje (según lo que decidiste en el Paso 3).
- Serializarlo y enviarlo usando el producer de Spring Kafka.

**Pistas de lo que necesitarás investigar:**
- ¿Qué clase de Spring Kafka usarías para enviar mensajes desde código Java? (es una plantilla, similar en espíritu a `JdbcTemplate`/`RestTemplate`).
- Esa clase suele tener métodos síncronos y asíncronos (devuelven un `Future`/`CompletableFuture`). ¿Cuál te conviene aquí y por qué?
- Como tus eventos son distintos records (`WalletCreated`, `MoneyDeposited`, ...) sin un campo común de "tipo", ¿cómo vas a discriminar qué tipo de evento es cada uno al serializar? (pista: `instanceof`/`switch` con pattern matching de Java 21, ya que tu proyecto usa esa versión).

### Cablear el bean

**Pregunta:** ¿Cómo evitas que el resto del código (los use cases) sepa que la implementación es Kafka? (respuesta: ya lo tienes resuelto arquitectónicamente — solo necesitas registrar `KafkaDomainEventPublisher` como el bean que satisface el tipo `DomainEventPublisher`, igual que hiciste manualmente con `WalletRepository` en `FinflowApiApplication`, o anotándolo como `@Component` si prefieres ese camino).

Este paso además resuelve, de paso, el hallazgo #1 de `docs/architecture-findings.md` (el puerto `DomainEventPublisher` sin implementación que probablemente impide arrancar la app).

---

## Paso 5: Probar el flujo de publicación

### Tarea

Sin escribir ningún consumer todavía, valida que los eventos realmente llegan a Kafka:

1. Levanta la aplicación.
2. Haz una petición HTTP real (p. ej. `POST /api/wallets` o `/deposit`) contra tu `WalletController`.
3. Desde la terminal, usa las herramientas de consola de tu contenedor Kafka para leer los mensajes del topic y confirmar que el evento llegó con la forma que esperabas.

**Pregunta:** Si el mensaje no llega, ¿cómo distingues si el problema es de serialización, de configuración de conexión, o de que el use case nunca llegó a llamar `publish(...)`?

---

## Paso 6 (opcional, para más adelante): Consumir eventos

Una vez que publicar funcione, el siguiente aprendizaje natural es **consumir** esos eventos — ya sea dentro del mismo servicio (p. ej. para un log de auditoría) o simulando un segundo microservicio que reacciona a `WalletCreated`.

No lo desarrolles todavía, pero piensa en:
- ¿Necesitarías un adaptador de *entrada* nuevo (un "Kafka Listener") en `infrastructure/messaging/`?
- ¿Ese listener llamaría directamente a un use case, o dispararía otro flujo distinto?
- ¿Qué pasa si el mismo evento llega dos veces? (idempotencia — tema grande, pero vale la pena que lo tengas en mente).

---

## Orden de Construcción Recomendado

1. Dependencia `spring-kafka` en el `pom.xml`.
2. Configuración de conexión en `application.properties` + verificación manual con las herramientas de consola de Kafka.
3. Decisión sobre modelo de eventos de integración (topics, keys, forma del mensaje).
4. `KafkaDomainEventPublisher` implementando `DomainEventPublisher`.
5. Cablear el bean para que los use cases lo usen sin cambios.
6. Probar end-to-end con una petición HTTP real y verificación manual del mensaje en el topic.
7. (Opcional) Consumer de prueba.

---

## Preguntas para Reflexionar

1. ¿Qué garantías pierdes/ganas si publicas el evento *después* de guardar en PostgreSQL en vez de hacerlo de forma transaccional (problema conocido como "dual write")? ¿Has oído del patrón *Outbox*?
2. Si `KafkaDomainEventPublisher.publish(...)` falla (broker caído, por ejemplo), ¿qué debería pasar con la operación del use case que ya guardó el `Wallet` en la base de datos?
3. ¿Los eventos de Kafka deberían incluir el estado completo de la wallet, o solo lo mínimo necesario (event-carried state transfer vs. eventos "finos")?
4. ¿Cómo versionarías el esquema de un evento si en el futuro necesitas agregar un campo nuevo sin romper a los consumidores existentes?

---

## Recursos de Estudio

- Documentación oficial de Spring for Apache Kafka (referencia para `KafkaTemplate`, configuración de producer/consumer).
- Documentación de Apache Kafka sobre conceptos core: topics, particiones, producers, consumers, consumer groups.
- Búsqueda: "Transactional Outbox Pattern" — relevante para el problema de "dual write" mencionado en las preguntas de reflexión.
- Búsqueda: "Domain Events vs Integration Events" (Vaughn Vernon / varios blogs de DDD) — para profundizar en la distinción de la sección de teoría.
