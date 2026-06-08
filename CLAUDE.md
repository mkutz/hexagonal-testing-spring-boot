# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

This is the companion codebase for the article in `README.md`, "Testing Hexagonal Architecture by Risk." It is a deliberately small Spring Boot / Kotlin application whose primary purpose is to **demonstrate a testing strategy**, not to ship a product. Treat `README.md` as the spec: the code exists to make its claims concrete, so changes that contradict the article's testing philosophy are almost always wrong. When in doubt, re-read the relevant Part of the README.

## Commands

```bash
./gradlew test               # Unit suite only — fast, no Spring, no Docker
./gradlew integrationTest    # Integration + e2e + approval suite — needs Docker (Testcontainers)
./gradlew check              # Both suites + Spotless verification
./gradlew spotlessApply      # Auto-format Kotlin, Gradle KTS, libs.versions.toml, and *.md
./gradlew bootRun            # Run the app (expects a Postgres + payment service; see app props)

# Run a single test class or method (either suite):
./gradlew test --tests "*PlaceOrderTest"
./gradlew integrationTest --tests "*JpaOrderRepositoryIntegrationTest"
./gradlew test --tests "*PlaceOrderTest.placing an order below the credit limit succeeds"
```

Approval tests (ApproveJ): a changed contract produces a `*-received.*` file next to the
`*-approved.*` snapshot and fails. Review the diff, then approve intentionally — never blindly
overwrite. The approvej Gradle tasks are wired to the `integrationTest` source set (see the
`JavaExec` block in `build.gradle.kts`), because the approval test lives there, not in `test`.

The Gradle toolchain runs on JDK 25 but compiles to **JVM 24 bytecode** (Kotlin 2.2.21 caps at
target 24). Keep `options.release` and `jvmTarget` in lockstep if you touch them.

## Architecture: ports and adapters

The package layout *is* the hexagon. Respect these boundaries — they are what makes the testing
strategy work:

- `application/order/` — the **core**: domain (`Order`, `Money`, `CustomerId`, `OrderId`) and the
  `PlaceOrder` use case. Depends only on port interfaces, never on Spring infrastructure, JPA, or
  HTTP. `Money` is stored/compared in minor units (cents).
- `application/order/port/` — the **driven ports**: `OrderRepository`, `PaymentGateway`. The core
  owns these interfaces; adapters implement them.
- `adaptersdriving/web/` — the **driving adapter**: `OrderController` and its request/response DTOs.
- `drivenadapters/persistence/` — JPA adapter (`JpaOrderRepository` + `OrderEntity` +
  `SpringDataOrderRepository`) backed by Postgres.
- `drivenadapters/payment/` — `RestClientPaymentGateway`, an HTTP client to an external payment service.

The core must not import from `adaptersdriving`, `drivenadapters`, or Spring web/JPA. New behavior
goes in the core behind a port; new infrastructure goes in an adapter implementing a port.

## Testing strategy (the whole point — read README before changing tests)

Each risk is tested at the **lowest layer that can credibly mitigate it, and nowhere else**. Do not
re-test a risk at a higher layer. Concretely:

- **Unit tests** (`src/test/`) — exhaust business-logic risk: every equivalence class and boundary
  (credit-limit cases live here). State-based assertions only; **no mocking framework**. Ports are
  replaced by hand-written in-memory fakes from `src/testFixtures/` (`InMemoryOrderRepository`,
  `InMemoryPaymentGateway`). No Spring context.
- **Contract test suites** (`src/testFixtures/`, e.g. `OrderRepositoryContract`) — abstract classes
  encoding behavior every port implementation must satisfy. Run against *both* the in-memory fake
  (as a unit test) and the real adapter (in the integration suite). This is what keeps fakes honest;
  when you add a port method, extend its contract.
- **Integration tests** (`src/integrationTest/`) — driven adapters against **real infrastructure**:
  Postgres via Testcontainers, the payment service via WireMock. Few cases: the shared contract plus
  the handful of things only real infra reveals (column mapping, dialect SQL, serialization). Do
  *not* re-test business rules here.
- **End-to-end test** (`PlaceOrderEndToEndTest`) — drives the whole assembled app over real HTTP to
  cover wiring/config risk. One happy path per entry point. Not for business rules.
- **Approval tests** (`OrderResponseApprovalTest`) — pin the exact serialized shape of inbound/
  outbound interfaces so contracts can't drift silently.

`src/testFixtures/` (the `java-test-fixtures` source set) is shared by all suites; put fakes,
contract classes, and test-data builders (`TestData.kt`, e.g. `anOrder(...)`) there.

### Two rules that keep the suite reliable and fast

1. **Single shared Spring context.** Every integration/e2e test uses the exact pair
   `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@Import(TestcontainersConfiguration::class)`
   so Spring caches and reuses one context across the whole suite. Don't introduce divergent context
   configurations (extra `@MockBean`, different properties, `@DirtiesContext`) without a deliberate
   reason — each variation spins up another expensive context.
2. **Zero-precondition tests.** Tests share state in that one context, so: give every entity a random
   ID, never assert on row *counts*, never assume an empty starting state. Assert only on what *this*
   test created (e.g. "this order exists", never "exactly one order exists").

`TestcontainersConfiguration` provides the shared Postgres (`@ServiceConnection`) and WireMock
payment-server beans, wiring `payment.base-url` to WireMock's dynamic port.
