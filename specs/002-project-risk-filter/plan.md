# Implementation Plan: Filter Projects By Risk

**Branch**: `002-project-risk-filter` | **Date**: 2026-07-02 | **Spec**: `specs/002-project-risk-filter/spec.md`

**Input**: Feature specification from `specs/002-project-risk-filter/spec.md`

## Summary

Add an optional `risk` filter to the project listing flow. The public list keeps the existing filters (`name`, `status`, `managerId`) and adds `risk` with the existing values `BAIXO`, `MEDIO`, and `ALTO`. Risk remains calculated dynamically and is not persisted. The implementation will keep repository specifications for persisted filters and apply the calculated risk filter in the service while preserving correct pagination metadata.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4, Spring Web MVC, Spring Data JPA, Hibernate, springdoc-openapi

**Storage**: PostgreSQL with Flyway. No schema change for this feature.

**Testing**: JUnit 5, Mockito, AssertJ, Maven Wrapper, JaCoCo

**Target Platform**: REST API running locally or in Docker

**Project Type**: Backend web service

**Performance Goals**: Preserve existing list behavior for requests without `risk`. For requests with `risk`, keep pagination metadata correct and avoid new persistence fields.

**Constraints**: Risk must use existing dynamic calculation rules and must not be persisted. Existing filters must continue working alone or combined.

**Scale/Scope**: Existing `GET /api/projects` endpoint only.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Business rules remain in service/domain: PASS. Risk filtering will reuse calculated business rules and remain outside controller-only logic.
- Database changes through Flyway: PASS. No database change is planned.
- Testable public behavior: PASS. Unit coverage and a validation quickstart will cover the new list behavior.
- API contract discipline: PASS. The public query contract and README must be updated.
- Keep existing architecture: PASS. The feature stays in the current Spring Boot/Maven layout.

## Project Structure

### Documentation (this feature)

```text
specs/002-project-risk-filter/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── projects-list-risk-filter.md
└── checklists/
    └── requirements.md
```

### Source Code (repository root)

```text
src/main/java/com/code/portfolio/project/
├── domain/
│   └── RiskLevel.java
├── mapper/
│   └── ProjectMapper.java
├── repository/
│   ├── ProjectRepository.java
│   └── ProjectSpecifications.java
├── service/
│   └── ProjectService.java
└── web/
    └── ProjectController.java

src/test/java/com/code/portfolio/project/service/
└── ProjectServiceTest.java

README.md
```

**Structure Decision**: Use the existing single Spring Boot project layout. Update only the controller/service path, tests, and public documentation. Keep `ProjectSpecifications` for persisted fields; do not add a `risk` database field.

## Phase 0: Research

See `research.md`.

## Phase 1: Design & Contracts

See `data-model.md`, `contracts/projects-list-risk-filter.md`, and `quickstart.md`.

## Post-Design Constitution Check

- Business rules remain in service/domain: PASS.
- Database changes through Flyway: PASS, no migration required.
- Testable public behavior: PASS, tests are planned before implementation tasks.
- API contract discipline: PASS, contract and README updates are included.
- Keep existing architecture: PASS, no new framework or project structure.

## Complexity Tracking

No constitution violations.
