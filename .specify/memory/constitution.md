# Portfolio Projetos Constitution

## Core Principles

### I. Business Rules in Domain or Service
Business rules must live in `domain` or `service` classes. Controllers should only expose HTTP behavior, validation boundaries, and delegation.

### II. Database Changes Through Flyway
The database schema is owned by Flyway migrations under `src/main/resources/db/migration`. Hibernate must keep `ddl-auto=validate`; entity changes that require schema changes must include a new migration.

### III. Testable Public Behavior
Every feature that changes behavior must include focused automated tests. The default validation command is `.\mvnw.cmd verify`, including the configured JaCoCo check.

### IV. API Contract Discipline
Changes to public API requests, responses, filters, or status codes must update the relevant DTOs, OpenAPI annotations when applicable, and user-facing documentation such as README or Postman collection when the contract changes.

### V. Keep the Existing Architecture
Use Java 25, Spring Boot 4, Spring Web MVC, Spring Data JPA, PostgreSQL, Flyway, Spring Security, springdoc-openapi, Maven Wrapper, JUnit 5, Mockito, and JaCoCo. Do not introduce new frameworks for small features.

## Project Constraints

- Protected endpoints require HTTP Basic authentication.
- `/actuator/health`, Swagger/OpenAPI, and `/api/members-provider/**` remain public.
- Members are external/mock-provider data; the main project stores member IDs, not member records.
- Project risk is calculated dynamically from budget and expected duration; it is not persisted.
- Existing package boundaries under `com.code.portfolio` should be preserved.

## Development Workflow

1. Start each non-trivial change with a spec in `specs/`.
2. Create a technical plan before editing source code.
3. Break work into tasks that can be checked off.
4. Implement the smallest coherent slice.
5. Run `.\mvnw.cmd verify` before considering the change done.

## Governance

This constitution guides Spec Kit work for this repository. When a feature conflicts with these rules, document the reason in the feature plan before implementation.

**Version**: 1.0.0 | **Ratified**: 2026-07-02 | **Last Amended**: 2026-07-02
