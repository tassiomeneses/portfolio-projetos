# Feature Specification: Filter Projects By Risk

**Feature Branch**: `002-project-risk-filter`

**Created**: 2026-07-02

**Status**: Draft

**Input**: User description: "Adicionar filtro por risco na listagem de projetos. O endpoint GET /api/projects deve aceitar o parâmetro opcional risk com valores BAIXO, MEDIO ou ALTO. Quando risk for informado, a resposta deve conter apenas projetos cujo risco calculado seja igual ao risco informado. O risco deve continuar sendo calculado dinamicamente com RiskCalculator e não deve ser persistido no banco. Os filtros existentes name, status e managerId devem continuar funcionando."

## User Scenarios & Testing

### User Story 1 - Filter project list by calculated risk (Priority: P1)

As an authenticated portfolio user, I want to filter the project list by risk level so I can quickly focus on low, medium, or high risk projects without manually reviewing every project.

**Why this priority**: Risk is already visible in project responses and portfolio reporting. Filtering by risk turns that information into an actionable project discovery workflow.

**Independent Test**: Create or use projects that evaluate to `BAIXO`, `MEDIO`, and `ALTO`, list projects with each risk value, and confirm only projects matching the requested risk are returned.

**Acceptance Scenarios**:

1. **Given** the portfolio has projects with `BAIXO`, `MEDIO`, and `ALTO` calculated risk, **When** the user lists projects with `risk=ALTO`, **Then** only high risk projects are returned.
2. **Given** the portfolio has projects matching different names, statuses, managers, and risk levels, **When** the user combines `risk` with existing filters, **Then** only projects matching all informed filters are returned.
3. **Given** no project matches the requested risk, **When** the user lists projects with that risk, **Then** the response is an empty paginated result rather than an error.
4. **Given** the user does not inform `risk`, **When** the user lists projects, **Then** the existing list behavior is preserved.

### Edge Cases

- Invalid risk values must be rejected and must not return an unfiltered project list.
- Risk must reflect the current project budget and expected duration at listing time.
- Existing pagination and sorting behavior must remain available when filtering by risk.
- Existing filters `name`, `status`, and `managerId` must continue working alone or combined with `risk`.
- Risk must remain a calculated value and must not become a user-editable persisted field.

## Requirements

### Functional Requirements

- **FR-001**: The project list MUST accept an optional risk filter with values `BAIXO`, `MEDIO`, and `ALTO`.
- **FR-002**: When the risk filter is omitted, the project list MUST keep its current behavior.
- **FR-003**: When the risk filter is informed, the project list MUST include only projects whose current calculated risk equals the requested risk.
- **FR-004**: The risk filter MUST combine with `name`, `status`, and `managerId` using intersection semantics: a project is returned only when it satisfies every informed filter.
- **FR-005**: Invalid risk values MUST be rejected consistently with other invalid list filter values.
- **FR-006**: Pagination and sorting metadata MUST remain correct after applying the risk filter.
- **FR-007**: Risk MUST remain calculated from existing project data and MUST NOT be stored as a new persisted project attribute.
- **FR-008**: Automated tests MUST cover risk-only filtering, combined filtering, omitted risk behavior, and invalid risk handling when supported by the existing validation layer.

### Key Entities

- **Project**: Portfolio item returned by the list, with budget, dates, manager, status, and calculated risk.
- **Risk Level**: Business classification with values `BAIXO`, `MEDIO`, and `ALTO`.
- **Project List Result**: Paginated result set returned to users when browsing projects.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Users can retrieve only projects from a selected risk level in a single list request.
- **SC-002**: In test data containing all three risk levels, 100% of returned projects match the requested risk.
- **SC-003**: Existing project list scenarios without risk continue to return the same results as before.
- **SC-004**: Combined filters exclude unrelated projects in all acceptance scenarios.
- **SC-005**: Invalid risk input never produces a misleading unfiltered project list.

## Assumptions

- The accepted risk values are the same labels already exposed to users: `BAIXO`, `MEDIO`, and `ALTO`.
- Risk is evaluated using the same business rules already used in project responses and portfolio reports.
- An empty result for a valid risk filter is a successful response.
- No new user permissions are required beyond the existing access rules for listing projects.
