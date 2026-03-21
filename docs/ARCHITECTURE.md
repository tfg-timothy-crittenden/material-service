TFG Service — Architecture map

Overview
--------
This document describes the high-level layout of the application and where to find the code for each adapter and layer. The system follows a hexagonal-inspired structure:

- Domain (core business objects) — holds immutable domain models and business rules.
- Application (use-cases / ports) — defines inbound/outbound ports and service implementations (use-cases).
- Infrastructure (adapters) — framework-specific code: web controllers, persistence adapters, security, config.
- App entry — Spring Boot application class and runtime registration (Eureka, config, etc.).

Files included in this repo (examples)
-------------------------------------
- Domain model
  - src/main/java/com/timcritt/tfg/domain/model/TestItem.java

- Application
  - Inbound port: src/main/java/com/timcritt/tfg/application/port/inbound/TestUseCase.java
  - Outbound port: src/main/java/com/timcritt/tfg/application/port/outbound/TestRepositoryPort.java
  - Service (use-case impl): src/main/java/com/timcritt/tfg/application/service/TestUseCaseImpl.java
  - Exceptions: src/main/java/com/timcritt/tfg/application/exception/TestNotFoundException.java

- Infrastructure (adapters)
  - Web controller: src/main/java/com/timcritt/tfg/infrastructure/web/TestController.java
  - DTO mapper: src/main/java/com/timcritt/tfg/infrastructure/web/TestDtoMapper.java
  - Web exception handler (Spring): src/main/java/com/timcritt/tfg/infrastructure/web/GlobalExceptionHandler.java
  - Persistence adapter: src/main/java/com/timcritt/tfg/infrastructure/persistence/TestRepositoryAdapter.java
  - JPA entity & mapper: src/main/java/com/timcritt/tfg/infrastructure/persistence/TestEntityMapper.java
  - Security config: src/main/java/com/timcritt/tfg/infrastructure/security/SecurityConfig.java

- Entry point & infra
  - src/main/java/com/timcritt/tfg/TfgApplication.java
  - compose.yaml (dev DB), application.properties / application-dev.properties

How the pieces interact (request path)
--------------------------------------
1. HTTP request hits `TestController` (infrastructure/web). The controller accepts DTOs and validates input.
2. Controller uses `TestDtoMapper` to convert DTO -> domain (or creates simple primitives for inbound ports).
3. Controller calls the inbound port (`TestUseCase`) — typically injected as `TestServiceAdapter` via DI.
4. `TestUseCaseImpl` implements the business logic. It calls the outbound port `TestRepositoryPort` to read/write domain objects.
5. `TestRepositoryAdapter` (infrastructure/persistence) implements `TestRepositoryPort` using JPA/Hibernate. It maps domain objects to JPA entities with `TestEntityMapper` and executes DB queries.
6. Any domain or application-specific exception (e.g., `TestNotFoundException`) bubbles back to the controller and is converted to an HTTP response by `GlobalExceptionHandler` (in infrastructure/web), producing e.g. a 404 JSON response.
7. Security concerns (JWT/token) are enforced by `SecurityConfig` in `infrastructure/security` (or by the gateway in a microservices setup). In dev you can permit all traffic using a dev profile.

Rendering the diagram
---------------------
- Use an IDE PlantUML plugin (IntelliJ has PlantUML integrations) or run PlantUML (with Java) to render `docs/architecture.puml` to an image (SVG/PNG).


