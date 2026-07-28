# finflow-api

A **Wallet** microservice built as a learning project for **Domain-Driven Design** and **Hexagonal Architecture** (Ports & Adapters), on Spring Boot 4.1 / Java 21.

## Scope

The service manages wallets for a single aggregate: `Wallet`. A wallet belongs to an owner, holds a balance in a given currency, and can be created, deposited into, withdrawn from, frozen and reactivated. Every state change raises a domain event, which is translated and published to Kafka for other services to consume.

This is not a production system — it's a hands-on sandbox for practicing DDD tactical patterns, hexagonal layering, event publishing, and testing (unit, integration, and BDD) on top of a real (if small) domain.

## Architecture

Hexagonal / Ports & Adapters, with the dependency rule pointing inward: `infrastructure` depends on `application`, `application` depends on `domain`, and `domain` depends on nothing.

```
com.rubencamero.finflow
├── domain                          — framework-free business logic
│   ├── entity          Wallet (aggregate root)
│   ├── valueobject     Money, WalletId, OwnerId, WalletStatus
│   ├── event           DomainEvent (sealed) + WalletCreated, WalletActivated,
│   │                   WalletFrozen, MoneyDeposited, MoneyWithdrawn
│   └── exception       InvalidWalletException
│
├── application                     — orchestration, no framework/infra leakage
│   ├── command          CreateWalletCommand, DepositMoneyCommand, WithdrawMoneyCommand,
│   │                    FreezeWalletCommand, ActivateWalletCommand, GetWalletQuery
│   ├── port              WalletRepository, DomainEventPublisher   (interfaces)
│   └── usecase           CreateWalletUseCase, GetWalletUseCase, DepositMoneyUseCase,
│                         WithdrawMoneyUseCase, FreezeWalletUseCase, ActivateWalletUseCase
│
└── infrastructure                  — adapters (the only layer that knows about Spring/JPA/Kafka/HTTP)
    ├── api
    │   ├── WalletController         REST inbound adapter
    │   ├── dto                      Request/response records
    │   ├── mapper                   WalletApiMapper (DTO ↔ command/domain)
    │   └── exception                WalletExceptionHandler + ErrorResponse (@RestControllerAdvice)
    ├── persistence
    │   ├── entity                   WalletEntity (JPA)
    │   ├── mapper                   WalletMapper (domain ↔ JPA)
    │   └── repository               WalletJpaRepository (Spring Data), PostgresWalletRepository (outbound adapter)
    └── messaging
        ├── event                    WalletIntegrationEvent + one record per domain event + WalletEventType
        ├── mapper                   WalletEventMapper (domain event → integration event)
        └── KafkaDomainEventPublisher  outbound adapter, implements DomainEventPublisher
```

Ports (`WalletRepository`, `DomainEventPublisher`) are wired to their real implementations as `@Bean`s in `FinflowApiApplication`; use cases are plain `@Service` classes discovered by component scanning.

See [docs/architecture-findings.md](docs/architecture-findings.md) for a running log of design decisions, bugs found/fixed, and open items, and [docs/DDD_and_hexagonal.md](docs/DDD_and_hexagonal.md) / [docs/kafka-integration.md](docs/kafka-integration.md) for the step-by-step learning guides this project was built from.

## Tech stack

- **Spring Boot 4.1** (Spring Framework 7) / **Java 21**
- **PostgreSQL** — persistence (Spring Data JPA / Hibernate)
- **Apache Kafka** — domain event publishing (`spring-boot-starter-kafka`)
- **springdoc-openapi** — Swagger UI / OpenAPI spec generation
- **JUnit 5 + AssertJ + Mockito** — unit and integration tests
- **Cucumber** — BDD tests (Gherkin feature files)

## Endpoints

Base path: `/api/wallets`

| Method | Path | Description | Success | Error |
|---|---|---|---|---|
| `POST` | `/api/wallets` | Create a wallet (always starts `ACTIVE`) | `200` `WalletResponse` | — |
| `GET` | `/api/wallets/{walletId}` | Get a wallet by id | `200` `WalletResponse` | `409` if not found |
| `POST` | `/api/wallets/{walletId}/deposit` | Deposit money | `200` `MessageResponse` | `409` if wallet not found / frozen |
| `POST` | `/api/wallets/{walletId}/withdraw` | Withdraw money | `200` `MessageResponse` | `409` if not found / frozen / insufficient balance |
| `PUT` | `/api/wallets/{walletId}/freeze` | Freeze a wallet | `200` `MessageResponse` | `409` if not found / already frozen |
| `PUT` | `/api/wallets/{walletId}/activate` | Reactivate a wallet | `200` `MessageResponse` | `409` if not found / already active |

**`POST /api/wallets` request body:**
```json
{ "ownerId": "3fa85f64-5717-4562-b3fc-2c963f66afa6", "amount": 100.00, "currency": "USD" }
```

**`POST /deposit` / `POST /withdraw` request body:**
```json
{ "amount": 50.00, "currency": "USD" }
```

**Error response shape** (all business-rule violations, via `WalletExceptionHandler`):
```json
{ "timestamp": "2026-07-28T21:00:00", "status": 409, "error": "Conflict", "message": "Insufficient balance." }
```

All business errors currently map to `409 Conflict`, including "wallet not found" (which would ideally be `404`) — see the open item about exception granularity in [architecture-findings.md](docs/architecture-findings.md).

### API docs (Swagger)

With the app running: `http://localhost:8080/swagger-ui.html`

## Domain events → Kafka

Every state change on `Wallet` raises a domain event, which `KafkaDomainEventPublisher` translates into an integration event and publishes to a single topic:

- **Topic:** `wallet-events`
- **Key:** `walletId` (so all events for the same wallet stay ordered within a partition)
- Event types: `WALLET_CREATED`, `WALLET_ACTIVATED`, `WALLET_FROZEN`, `MONEY_DEPOSITED`, `MONEY_WITHDRAWN`

See [docs/kafka-integration.md](docs/kafka-integration.md) for the full design rationale.

## Running locally

**Prerequisites:**
- PostgreSQL running locally, database `wallet_microservice_db` (see `src/main/resources/application.properties` for credentials — schema is auto-created via `spring.jpa.hibernate.ddl-auto=update`)
- Kafka running locally on `localhost:9092` (e.g. `apache/kafka` Docker image)

```bash
./mvnw spring-boot:run
```

## Testing

```bash
./mvnw test
```

Runs the full suite in one pass — unit tests, Spring integration tests, and BDD scenarios all discovered by the same JUnit Platform run:

- **Unit tests** (`src/test/java/.../domain/`) — pure JUnit + AssertJ, no Spring: `WalletTest`, `MoneyTest`, `WalletIdTest`, `OwnerIdTest`.
- **Integration tests** (`src/test/java/.../application/usecase/`) — `@SpringBootTest` against a real Postgres, `DomainEventPublisher` mocked out (`@MockitoBean`) so Kafka isn't required, `@Transactional` rollback per test. One class per use case.
- **BDD tests** (`src/test/java/.../bdd/`, feature files in `src/test/resources/features/`) — Cucumber scenarios in Gherkin exercising the same use cases end-to-end against Postgres. Run only these with `./mvnw test -Dtest=RunCucumberTest`.

## Project docs

- [docs/architecture-findings.md](docs/architecture-findings.md) — architecture review log: what's solid, what's been fixed, what's still open.
- [docs/DDD_and_hexagonal.md](docs/DDD_and_hexagonal.md) — the guided learning path this project follows for DDD/hexagonal.
- [docs/kafka-integration.md](docs/kafka-integration.md) — Kafka integration theory and step-by-step guide.
