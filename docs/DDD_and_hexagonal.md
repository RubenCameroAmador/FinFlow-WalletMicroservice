# DDD y Arquitectura Hexagonal - Guía Wallet Service

## Estado Actual

Ya tienes construido:
- Entidad `Wallet` con `id`, `ownerId`, `balance` (falta `status` y `createdAt`)
- Value Objects: `Money`, `WalletId`, `UserId`
- Use Case: `CreateWalletUseCase`
- Puerto: `WalletRepository`

---

## Paso 1: Value Objects Faltantes

### ¿Qué es un Value Object?

Es un objeto inmutable que se identifica por sus **propiedades**, no por un ID. Dos value objects con los mismos valores son equivalentes.

### Tarea: Crear `Currency`

Ya usas `java.util.Currency` en `Money`. En DDD, los dominios deberían ser auto-contenidos.

**Pregunta para ti:** ¿Por qué sería mejor crear un value object `Currency` en vez de usar directamente `java.util.Currency`?

**Qué necesitas:**
- Un value object que encapsule el código de moneda (USD, EUR, etc.)
- Debe ser inmutable
- Debe implementar `equals()` y `hashCode()` basados en el valor

### Tarea: Crear `OwnerId`

Observa que ya tienes `UserId`. En el contexto del dominio Wallet, ¿tiene más sentido hablar de `OwnerId` o `UserId`?

**Opción:** Puedes renombrar `UserId` a `OwnerId` o crear un alias. La decisión depende de si `UserId` se usará en otros bounded contexts.

---

## Paso 2: Agregar Campos a la Entidad Wallet

### `status` - Estado de la Wallet

El estado controla qué operaciones son válidas. Piensa en un simple enum o value object.

**Estados posibles:**
- `ACTIVE` - La wallet puede recibir y enviar dinero
- `FROZEN` - La wallet solo puede recibir dinero, no puede enviar

**Pregunta:** ¿Dónde debería vivir la validación de estado? ¿En la entidad Wallet o en los use cases?

### `createdAt` - Marca de tiempo

**Pregunta:** ¿Debería ser `LocalDateTime` o un value object `CreatedAt`? ¿Por qué?

---

## Paso 3: Completar la Entidad Wallet con Comandos

### Métodos `freeze()` y `activate()`

**Reglas de negocio a considerar:**
- ¿Se puede congelar una wallet ya congelada?
- ¿Se puede activar una wallet ya activa?
- ¿Qué pasa si intentas withdraw de una wallet congelada?

**Tarea:** Agrega los campos `status` y `createdAt` a la entidad Wallet. Actualiza el constructor para inicializarlos.

---

## Paso 4: Eventos de Dominio

### ¿Qué son los Eventos de Dominio?

Son objetos inmutables que representan algo significativo que pasó en el dominio. **No modifican el estado** del aggregate, solo lo registran.

### Eventos a crear:

1. `WalletCreated` - Cuando se crea una wallet nueva
2. `MoneyDeposited` - Cuando se deposita dinero
3. `MoneyWithdrawn` - Cuando se retira dinero
4. `WalletFrozen` - Cuando se congela una wallet
5. `WalletActivated` - Cuando se activa una wallet

**Estructura típica de un evento:**
- `eventId` (identificador único)
- `occurredOn` (timestamp)
- Datos del evento (walletId, amount, etc.)

**Pregunta:** ¿Dónde se almacenan temporalmente los eventos? ¿En la entidad Wallet o en un "event store" externo?

### Tarea:

1. Crea la clase `DomainEvent` base (abstracta o interfaz)
2. Crea cada evento como record o clase inmutable
3. Modifica la entidad `Wallet` para que tenga una lista de eventos
4. Agrega un método `pullEvents()` o `clearEvents()` para obtener y limpiar los eventos pendientes

---

## Paso 5: Use Cases Faltantes

Cada use case representa una **command** del usuario. Ya tienes `CreateWalletUseCase`.

### Use Cases a crear:

| Use Case | Command | Qué hace |
|----------|---------|----------|
| `DepositMoneyUseCase` | `DepositMoneyCommand` | Deposita dinero en una wallet existente |
| `WithdrawMoneyUseCase` | `WithdrawMoneyCommand` | Retira dinero de una wallet existente |
| `FreezeWalletUseCase` | `FreezeWalletCommand` | Congela una wallet |
| `ActivateWalletUseCase` | `ActivateWalletCommand` | Activa una wallet congelada |
| `GetWalletUseCase` | `GetWalletQuery` | Obtiene una wallet por ID |

### Patrón de cada Use Case:

```
1. Recibe un Command/Query
2. Usa el Repository para obtener la entidad
3. Ejecuta la operación en la entidad
4. Guarda los cambios con el Repository
5. Retorna un resultado (la wallet o un void)
```

**Pregunta:** ¿Qué diferencia hay entre un Command y un Query en CQRS?

---

## Paso 6: Puerto de Salida para Eventos (Opcional)

### ¿Para qué sirve?

Permite que capas externas (infraestructura) reaccionen a los eventos sin acoplar el dominio.

```java
public interface DomainEventPublisher {
    void publish(List<DomainEvent> events);
}
```

**Pregunta:** ¿Cuándo se publican los eventos? ¿Dentro del use case o después?

---

## Paso 7: Validación de Reglas de Negocio

### Ubicación de validaciones:

| Qué validar | Dónde |
|-------------|-------|
| Monto no negativo | Value Object `Money` |
| Moneda coincidente | Value Object `Money` |
| Wallet existe | Use Case (antes de operar) |
| Wallet tiene fondos suficientes | Entidad `Wallet` |
| Wallet no está congelada | Entidad `Wallet` |

### Tarea:

Revisa tu entidad `Wallet`. ¿Dónde están las validaciones de negocio actualmente? ¿Están todas en el lugar correcto según la tabla anterior?

---

## Estructura de Paquetes Propuesta

```
src/main/java/com/rubencamero/finflow/
├── domain/
│   ├── entity/
│   │   └── Wallet.java
│   ├── valueobject/
│   │   ├── Money.java
│   │   ├── WalletId.java
│   │   ├── OwnerId.java (o UserId.java)
│   │   ├── Currency.java
│   │   ├── WalletStatus.java
│   │   └── CreatedAt.java
│   ├── event/
│   │   ├── DomainEvent.java
│   │   ├── WalletCreated.java
│   │   ├── MoneyDeposited.java
│   │   ├── MoneyWithdrawn.java
│   │   ├── WalletFrozen.java
│   │   └── WalletActivated.java
│   └── exception/
│       ├── InvalidWalletException.java
│       └── WalletNotFoundException.java
├── application/
│   ├── command/
│   │   ├── CreateWalletCommand.java
│   │   ├── DepositMoneyCommand.java
│   │   ├── WithdrawMoneyCommand.java
│   │   ├── FreezeWalletCommand.java
│   │   ├── ActivateWalletCommand.java
│   │   └── GetWalletQuery.java
│   ├── usecase/
│   │   ├── CreateWalletUseCase.java
│   │   ├── DepositMoneyUseCase.java
│   │   ├── WithdrawMoneyUseCase.java
│   │   ├── FreezeWalletUseCase.java
│   │   ├── ActivateWalletUseCase.java
│   │   └── GetWalletUseCase.java
│   └── port/
│       ├── WalletRepository.java
│       └── DomainEventPublisher.java (opcional)
└── infrastructure/ (cuando agregues Spring)
    └── ...
```

---

## Orden de Construcción Recomendado

1. **Value Objects** primero (son la base)
2. **Entidad Wallet** completa (con status, createdAt, freeze, activate)
3. **Eventos de Dominio**
4. **Commands y Queries**
5. **Use Cases**
6. **Tests** (unitarias para dominio)

---

## Preguntas para Reflexionar

1. ¿Los eventos de dominio se generan dentro de la entidad o en el use case?
2. ¿Debería el Repository tener métodos `update()` aparte de `save()`?
3. ¿Cómo manejas el caso de que una wallet no existe al intentar depositar?
4. ¿El `GetWalletUseCase` debería usar el mismo `WalletRepository` que los commands?

---

## Recursos de Estudio

- **Libro:** "Domain-Driven Design" de Eric Evans (capítulos 5 y 6 sobre Value Objects y Entities)
- **Libro:** "Implementing Domain-Driven Design" de Vaughn Vernon
- **Blog:** "Domain Events - Greg Young" (búsqueda en Google)

---
---

# FASE 2: INFRAESTRUCTURA CON SPRING

---

## Estado Actual - Fase 2

El dominio y la capa de aplicación están completos:

```
domain/          ✅ Entidad, Value Objects, Eventos, Excepciones
application/     ✅ Use Cases, Commands, Ports (interfaces)
infrastructure/  ⏳ Pendiente
```

**Lo que falta:** Implementar los **adaptadores** que conectan tu dominio con el mundo real (HTTP, base de datos).

---

## ¿Qué es un Adaptador en Arquitectura Hexagonal?

```
                    ┌─────────────────────────┐
                    │      REST Controller     │  ← Adaptador de entrada
                    │      (HTTP Request)      │
                    └────────────┬────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │       Use Case          │  ← Lógica de negocio
                    └────────────┬────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
         ┌──────────────────┐      ┌──────────────────┐
         │  WalletRepository│      │DomainEventPublisher│  ← Ports (interfaces)
         │  (puerto salida) │      │  (puerto salida)  │
         └────────┬─────────┘      └────────┬─────────┘
                  │                         │
                  ▼                         ▼
         ┌──────────────────┐      ┌──────────────────┐
         │ JPA Repository   │      │ Logging Publisher │  ← Adaptadores de salida
         │ (PostgreSQL)     │      │ (o Kafka, etc.)  │
         └──────────────────┘      └──────────────────┘
```

**Los adaptadores implementan las interfaces (puertos) que definiste en la capa de aplicación.**

---

## Paso 8: Configuración de Spring Boot

### 8.1 Dependencias (ya en tu pom.xml)

Ya tienes:
- `spring-boot-starter-data-jpa` — JPA/Hibernate para persistencia
- `spring-boot-starter-webmvc` — REST API
- `postgresql` — Driver de PostgreSQL
- `lombok` — Para reducir boilerplate

### 8.2 Archivo de configuración

**Ubicación:** `src/main/resources/application.properties`

**Qué necesita configurar:**
- URL de conexión a PostgreSQL
- Usuario y contraseña de la BD
- Propiedades de JPA (hibernate.ddl-auto, show-sql, etc.)

**Pregunta para ti:** ¿Qué valor le ponds a `hibernate.ddl-auto`?

| Valor | Qué hace |
|-------|----------|
| `create` | Borra y recrea la BD en cada inicio |
| `create-drop` | Crea al inicio, borra al cerrar |
| `update` | Actualiza schema sin borrar datos |
| `validate` | Solo valida, no modifica |
| `none` | No hace nada (producción) |

---

## Paso 9: Mapeo Dominio ↔ JPA (Entidad de Persistencia)

### El problema

Tu entidad `Wallet` del dominio **no debería** tener anotaciones de JPA (`@Entity`, `@Id`, etc.). En DDD, el dominio es puro y no depende de infraestructura.

**Solución:** Crear una **entidad de persistencia** separada que se mapea a la tabla de la BD.

### Paquete: `infrastructure/persistence/entity/`

```
infrastructure/
└── persistence/
    └── entity/
        └── WalletEntity.java    ← entidad JPA (se mapea a la tabla)
```

### `WalletEntity.java`

**Qué lleva:**
- Anotación `@Entity` y `@Table(name = "wallets")`
- `@Id` con `@GeneratedValue` para el ID
- Campos que mapean a columnas: `ownerId`, `balance`, `currency`, `status`, `createdAt`
- **NO tiene** lógica de negocio (sin validaciones, sin métodos freeze/activate)

**Pregunta:** ¿Cómo mapeas `Money` (que tiene `amount` y `currency`) a columnas de la BD?

**Opciones:**
1. Dos columnas separadas: `balance_amount` (BigDecimal) y `balance_currency` (String)
2. Una columna JSON: `balance` (JSONB en PostgreSQL)

---

## Paso 10: Mapeo entre Entidades

### Wallet (dominio) ↔ WalletEntity (JPA)

Necesitas convertir entre ambas. Hay dos enfoques:

**Opción A: Mapper estático**

```java
public class WalletMapper {
    public static WalletEntity toEntity(Wallet wallet) { ... }
    public static Wallet toDomain(WalletEntity entity) { ... }
}
```

**Opción B: Métodos dentro de la entidad JPA**

```java
public class WalletEntity {
    public Wallet toDomain() { ... }
    public static WalletEntity fromDomain(Wallet wallet) { ... }
}
```

**Pregunta:** ¿Cuál prefieres? La opción A mantiene las entidades limpias. La opción B es más compacta.

---

## Paso 11: Implementar el Repository (Adaptador de Salida)

### Paquete: `infrastructure/persistence/`

```
infrastructure/
└── persistence/
    ├── entity/
    │   └── WalletEntity.java
    ├── WalletJpaRepository.java      ← interfaz Spring Data
    └── PostgresWalletRepository.java ← implementación de tu puerto
```

### `WalletJpaRepository.java`

Extiende `JpaRepository` de Spring Data. Provee métodos CRUD automáticos.

### `PostgresWalletRepository.java`

Implementa tu interfaz `WalletRepository` del puerto.

**Qué hace cada método:**

| Método | Flujo |
|--------|-------|
| `save(wallet)` | Convierte a `WalletEntity` → llama `jpaRepository.save()` → convierte de vuelta a `Wallet` |
| `findById(id)` | Llama `jpaRepository.findById()` → convierte a `Wallet` → retorna `Optional<Wallet>` |

**Pregunta:** ¿El `save()` debería manejar create y update, o necesitas métodos separados?

---

## Paso 12: Configurar Spring Data JPA

### Paquete: `infrastructure/config/`

```
infrastructure/
└── config/
    └── RepositoryConfig.java   ← configura los beans de Spring
```

**Qué necesita:**
- Anotación `@Configuration`
- Anotación `@EnableJpaRepositories` apuntando al paquete de tus repos JPA
- Anotación `@EntityScan` apuntando al paquete de tus entidades JPA
- Bean para `WalletRepository` que inyecte el `WalletJpaRepository`

**Pregunta:** ¿Cómo le dices a Spring que cuando alguien pida `WalletRepository` (tu interfaz del puerto) le des `PostgresWalletRepository` (la implementación)?

---

## Paso 13: Adaptador de Entrada — REST Controller

### Paquete: `infrastructure/api/`

```
infrastructure/
└── api/
    ├── WalletController.java      ← expone endpoints HTTP
    ├── dto/
    │   ├── CreateWalletRequest.java   ← lo que el cliente envía
    │   ├── WalletResponse.java        ← lo que el cliente recibe
    │   ├── DepositRequest.java
    │   └── WithdrawRequest.java
    └── mapper/
        └── WalletApiMapper.java       ← convierte DTO ↔ Command
```

### `WalletController.java`

**Qué lleva:**
- Anotación `@RestController` y `@RequestMapping("/api/wallets")`
- Inyección de los Use Cases (NO el Repository directamente)
- Un método por cada operación

**Endpoints:**

| Método HTTP | Ruta | Use Case | Descripción |
|-------------|------|----------|-------------|
| `POST` | `/api/wallets` | `CreateWalletUseCase` | Crear wallet |
| `GET` | `/api/wallets/{id}` | `GetWalletUseCase` | Obtener wallet |
| `POST` | `/api/wallets/{id}/deposit` | `DepositMoneyUseCase` | Depositar dinero |
| `POST` | `/api/wallets/{id}/withdraw` | `WithdrawMoneyUseCase` | Retirar dinero |
| `PUT` | `/api/wallets/{id}/freeze` | `FreezeWalletUseCase` | Congelar wallet |
| `PUT` | `/api/wallets/{id}/activate` | `ActivateWalletUseCase` | Activar wallet |

### DTOs (Data Transfer Objects)

**`CreateWalletRequest`** — Lo que el cliente envía:
```json
{
  "ownerId": "uuid-del-owner",
  "initialBalance": {
    "amount": 1000.00,
    "currency": "USD"
  }
}
```

**`WalletResponse`** — Lo que el cliente recibe:
```json
{
  "id": "uuid-de-la-wallet",
  "ownerId": "uuid-del-owner",
  "balance": 1000.00,
  "currency": "USD",
  "status": "ACTIVE",
  "createdAt": "2026-07-20T10:30:00"
}
```

### Flujo completo de una petición HTTP

```
1. Cliente envía POST /api/wallets con JSON
2. Controller recibe CreateWalletRequest (DTO)
3. Controller convierte DTO → CreateWalletCommand
4. Controller llama a CreateWalletUseCase.execute(command)
5. Use Case crea Wallet (dominio)
6. Use Case guarda con WalletRepository
7. Use Case publica eventos con DomainEventPublisher
8. Use Case retorna Wallet
9. Controller convierte Wallet → WalletResponse (DTO)
10. Controller retorna JSON al cliente
```

---

## Paso 14: Manejo de Errores

### Paquete: `infrastructure/api/`

```
infrastructure/
└── api/
    └── WalletExceptionHandler.java  ← maneja excepciones del dominio
```

**Qué hace:**
- Captura `InvalidWalletException` del dominio
- Convierte a una respuesta HTTP con código adecuado (400, 404, etc.)
- Retorna JSON con mensaje de error

**Pregunta:** ¿Cómo le dices a Spring que cuando lancen `InvalidWalletException` retorne un 400 en vez de un 500?

---

## Paso 15: Puerto para Eventos (Implementación)

### Paquete: `infrastructure/event/`

```
infrastructure/
└── event/
    └── LoggingEventPublisher.java   ← implementa DomainEventPublisher
```

**Qué hace:**
- Implementa `DomainEventPublisher`
- Por ahora, solo imprime los eventos en consola (logger)
- Más adelante puede enviar a Kafka, RabbitMQ, etc.

---

## Estructura Final de Paquetes

```
src/main/java/com/rubencamero/finflow/
├── domain/
│   ├── entity/
│   │   └── Wallet.java
│   ├── valueobject/
│   │   ├── Money.java
│   │   ├── WalletId.java
│   │   ├── OwnerId.java
│   │   └── WalletStatus.java
│   ├── event/
│   │   ├── DomainEvent.java
│   │   └── ... (eventos)
│   └── exception/
│       └── InvalidWalletException.java
│
├── application/
│   ├── command/
│   │   └── ... (commands)
│   ├── usecase/
│   │   └── ... (use cases)
│   └── port/
│       ├── WalletRepository.java
│       └── DomainEventPublisher.java
│
└── infrastructure/
    ├── config/
    │   └── RepositoryConfig.java
    ├── persistence/
    │   ├── entity/
    │   │   └── WalletEntity.java
    │   ├── WalletJpaRepository.java
    │   └── PostgresWalletRepository.java
    ├── api/
    │   ├── WalletController.java
    │   ├── WalletExceptionHandler.java
    │   ├── dto/
    │   │   ├── CreateWalletRequest.java
    │   │   ├── WalletResponse.java
    │   │   ├── DepositRequest.java
    │   │   └── WithdrawRequest.java
    │   └── mapper/
    │       └── WalletApiMapper.java
    └── event/
        └── LoggingEventPublisher.java
```

---

## Orden de Construcción - Fase 2

1. **Configuración** — `application.properties`
2. **Entidad JPA** — `WalletEntity.java`
3. **Mapper dominio ↔ JPA** — Conversión entre entidades
4. **Repository JPA** — `WalletJpaRepository` + `PostgresWalletRepository`
5. **Config Spring** — `RepositoryConfig.java`
6. **Event Publisher** — `LoggingEventPublisher`
7. **DTOs** — Request/Response objects
8. **Controller** — `WalletController.java`
9. **Manejo de errores** — `WalletExceptionHandler`
10. **Test** — Probar con PostgreSQL real

---

## Relación: Dominio ↔ BD ↔ Controladores

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENTE (Postman, Frontend)              │
└───────────────────────────────┬─────────────────────────────────┘
                                │ HTTP Request
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│  infrastructure/api/WalletController.java                       │
│  (recibe JSON → convierte a Command → llama UseCase)            │
└───────────────────────────────┬─────────────────────────────────┘
                                │ Command
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│  application/usecase/CreateWalletUseCase.java                   │
│  (orquesta: crear entidad → guardar → publicar eventos)         │
└──────────┬──────────────────────────────────────┬───────────────┘
           │                                      │
           ▼                                      ▼
┌──────────────────────┐              ┌──────────────────────────┐
│ WalletRepository     │              │ DomainEventPublisher     │
│ (puerto/salida)      │              │ (puerto/salida)          │
└──────────┬───────────┘              └──────────┬───────────────┘
           │ implementa                          │ implementa
           ▼                                      ▼
┌──────────────────────┐              ┌──────────────────────────┐
│ PostgresWallet       │              │ LoggingEventPublisher    │
│ Repository           │              │ (imprime en consola)     │
│ (adapta a JPA)       │              │                          │
└──────────┬───────────┘              └──────────────────────────┘
           │ usa
           ▼
┌──────────────────────┐
│ WalletJpaRepository  │  ← Spring Data JPA
│ (JpaRepository)      │
└──────────┬───────────┘
           │ ejecuta SQL
           ▼
┌──────────────────────┐
│   PostgreSQL         │
│   (base de datos)    │
└──────────────────────┘
```

**Punto clave:** El **dominio no sabe** que existe PostgreSQL, Spring, ni HTTP. Solo conoce sus entidades, value objects y las interfaces (puertos).
