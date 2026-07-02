# Feature Specification: Portfolio Report Risk Summary

**Feature Branch**: `001-report-risk-summary`

**Created**: 2026-07-02

**Status**: Draft

**Input**: User description: "Adicionar ao relatorio do portfolio a quantidade de projetos por nivel de risco calculado"

## User Scenarios & Testing

### User Story 1 - See project counts by risk in the portfolio report (Priority: P1)

As an authenticated API consumer, I want the portfolio report to include the amount of projects by calculated risk level so I can understand the portfolio exposure without querying each project individually.

**Why this priority**: The report already summarizes status, budget, duration, and allocated members. Risk distribution is a natural missing summary for portfolio management.

**Independent Test**: Can be tested by generating a portfolio report with projects that evaluate to `BAIXO`, `MEDIO`, and `ALTO` and checking the returned `quantidadePorRisco` map.

**Acceptance Scenarios**:

1. **Given** projects with low, medium, and high calculated risk, **When** the portfolio report is generated, **Then** the response includes the correct count for each risk level present.
2. **Given** no projects for a risk level, **When** the portfolio report is generated, **Then** that risk level may be absent from the map rather than returned with zero.
3. **Given** no projects, **When** the portfolio report is generated, **Then** `quantidadePorRisco` is an empty map.

### Edge Cases

- Projects with the same status but different risk levels must be counted independently by risk.
- Risk must use the existing `RiskCalculator` rules and must not be persisted.
- Existing fields in the portfolio report must keep their current names and behavior.

## Requirements

### Functional Requirements

- **FR-001**: The system MUST add `quantidadePorRisco` to `GET /api/reports/portfolio`.
- **FR-002**: The system MUST calculate risk using the existing budget and expected duration rules.
- **FR-003**: The system MUST aggregate counts by `RiskLevel`.
- **FR-004**: The system MUST preserve the existing report fields: `quantidadePorStatus`, `totalOrcadoPorStatus`, `mediaDuracaoDiasProjetosEncerrados`, and `totalMembrosUnicosAlocados`.
- **FR-005**: The system MUST not add a database column or migration for risk.
- **FR-006**: The system MUST include automated tests for risk aggregation.

### Key Entities

- **Project**: Existing persisted project entity containing budget, start date, expected end date, status, manager, and members.
- **RiskLevel**: Existing enum with `BAIXO`, `MEDIO`, and `ALTO`.
- **PortfolioReportResponse**: API response returned by the portfolio report endpoint.

## Success Criteria

### Measurable Outcomes

- **SC-001**: A report generated from three projects with distinct risk levels returns one count for each risk level.
- **SC-002**: Existing report aggregation tests continue to pass without changing current field semantics.
- **SC-003**: `.\mvnw.cmd verify` completes successfully.

## Assumptions

- Risk distribution is needed only in the existing portfolio report endpoint.
- No historical risk tracking is required.
- Fetching current project records to calculate risk is acceptable for this portfolio summary.
