# Implementation Plan: Portfolio Report Risk Summary

**Branch**: `001-report-risk-summary` | **Date**: 2026-07-02 | **Spec**: `specs/001-report-risk-summary/spec.md`

**Input**: Feature specification from `specs/001-report-risk-summary/spec.md`

## Summary

Add a `quantidadePorRisco` field to the portfolio report response. The service will reuse `RiskCalculator`, load current projects, calculate each project's risk, and aggregate counts in an `EnumMap<RiskLevel, Long>`.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4, Spring Web MVC, Spring Data JPA, springdoc-openapi

**Storage**: PostgreSQL with Flyway. No schema change for this feature.

**Testing**: JUnit 5, Mockito, AssertJ, Maven Wrapper

**Target Platform**: REST API running locally or in Docker

**Project Type**: Backend web service

**Performance Goals**: Keep behavior simple and consistent with dynamic risk calculation. No new query optimization until report volume requires it.

**Constraints**: Risk is calculated dynamically and must not be persisted.

**Scale/Scope**: Existing portfolio report endpoint only.

## Constitution Check

- Business rules remain in service/domain: PASS.
- No database migration is needed because risk remains calculated: PASS.
- Automated tests will cover changed behavior: PASS.
- Public API response changes require README update: PASS.
- Existing Spring/Maven architecture is preserved: PASS.

## Project Structure

### Documentation

```text
specs/001-report-risk-summary/
├── spec.md
├── plan.md
├── tasks.md
└── contracts/
    └── portfolio-report-risk-summary.md
```

### Source Code

```text
src/main/java/com/code/portfolio/project/
├── dto/PortfolioReportResponse.java
└── service/PortfolioReportService.java

src/test/java/com/code/portfolio/project/service/
└── PortfolioReportServiceTest.java

README.md
```

**Structure Decision**: Keep the current single Spring Boot project layout and update only the report DTO, report service, tests, and README.

## Complexity Tracking

No constitution violations.
