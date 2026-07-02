# Tasks: Filter Projects By Risk

**Input**: Design documents from `specs/002-project-risk-filter/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/projects-list-risk-filter.md`, `quickstart.md`

**Tests**: Tests are required by the feature specification and by the project constitution because this changes public list behavior.

**Organization**: Tasks are grouped by user story so the feature can be implemented and validated as one independently testable slice.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel with other tasks that touch different files.
- **[Story]**: Maps the task to the user story from `spec.md`.
- Each task includes the exact file path to update or validate.

## Phase 1: Setup

**Purpose**: Confirm the active feature context and existing implementation points before editing.

- [X] T001 Confirm `.specify/feature.json` points to `specs/002-project-risk-filter`
- [X] T002 Review existing project list flow in `src/main/java/com/code/portfolio/project/web/ProjectController.java`
- [X] T003 Review existing project list service flow in `src/main/java/com/code/portfolio/project/service/ProjectService.java`
- [X] T004 Review existing project list tests in `src/test/java/com/code/portfolio/project/service/ProjectServiceTest.java`

---

## Phase 2: Foundational

**Purpose**: No new shared infrastructure is required; preserve existing architecture and avoid database changes.

- [X] T005 Confirm no Flyway migration is required under `src/main/resources/db/migration`
- [X] T006 Confirm `RiskLevel` and `RiskCalculator` already cover allowed risk values in `src/main/java/com/code/portfolio/project/domain/`

**Checkpoint**: Foundation ready. User story implementation can begin.

---

## Phase 3: User Story 1 - Filter project list by calculated risk (Priority: P1)

**Goal**: Allow authenticated users to filter `GET /api/projects` by calculated risk while keeping existing filters, pagination, and sorting behavior.

**Independent Test**: Create service-level test data with projects that calculate to `BAIXO`, `MEDIO`, and `ALTO`; call list with `risk=ALTO`; verify every returned item is high risk and pagination totals reflect only matching projects.

### Tests for User Story 1

- [X] T007 [P] [US1] Add risk-only filtering test in `src/test/java/com/code/portfolio/project/service/ProjectServiceTest.java`
- [X] T008 [P] [US1] Add combined `managerId` plus `risk` filtering test in `src/test/java/com/code/portfolio/project/service/ProjectServiceTest.java`
- [X] T009 [P] [US1] Add omitted-risk regression test for existing list behavior in `src/test/java/com/code/portfolio/project/service/ProjectServiceTest.java`
- [X] T010 [P] [US1] Add pagination metadata test for risk filtering in `src/test/java/com/code/portfolio/project/service/ProjectServiceTest.java`

### Implementation for User Story 1

- [X] T011 [US1] Update `ProjectService` constructor to receive `RiskCalculator` in `src/main/java/com/code/portfolio/project/service/ProjectService.java`
- [X] T012 [US1] Update `ProjectService.list` signature to accept `RiskLevel risk` in `src/main/java/com/code/portfolio/project/service/ProjectService.java`
- [X] T013 [US1] Preserve existing repository filtering for `name`, `status`, and `managerId` in `src/main/java/com/code/portfolio/project/service/ProjectService.java`
- [X] T014 [US1] Add calculated risk filtering and page slicing in `src/main/java/com/code/portfolio/project/service/ProjectService.java`
- [X] T015 [US1] Update `ProjectController.list` to receive optional `RiskLevel risk` in `src/main/java/com/code/portfolio/project/web/ProjectController.java`
- [X] T016 [US1] Update `ProjectController.list` to pass `risk` into `ProjectService.list` in `src/main/java/com/code/portfolio/project/web/ProjectController.java`
- [X] T017 [US1] Update OpenAPI operation summary to include `risk` in `src/main/java/com/code/portfolio/project/web/ProjectController.java`

**Checkpoint**: `GET /api/projects?risk=ALTO` is implemented and testable independently.

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: Keep public documentation and validation aligned with the API contract.

- [X] T018 [P] Update endpoint filter documentation in `README.md`
- [X] T019 [P] Add `risk` filter examples to `README.md`
- [X] T020 Run `.\mvnw.cmd verify` from repository root
- [X] T021 Confirm quickstart expectations in `specs/002-project-risk-filter/quickstart.md` still match the implemented behavior

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Setup completion.
- **User Story 1 (Phase 3)**: Depends on Foundational completion.
- **Polish (Phase 4)**: Depends on User Story 1 implementation.

### User Story Dependencies

- **User Story 1 (P1)**: No dependency on other stories; this is the complete MVP.

### Within User Story 1

- Tests T007-T010 should be written before implementation.
- `ProjectService` changes T011-T014 must happen before controller wiring T015-T017 can compile.
- Documentation T018-T019 should happen after contract behavior is settled.
- Verification T020 must happen after implementation and docs updates.

## Parallel Opportunities

- T002, T003, and T004 can be reviewed in parallel.
- T007, T008, T009, and T010 touch the same test file, so execute carefully in sequence despite being conceptually independent.
- T018 and T019 both touch `README.md`, so execute together or sequentially to avoid conflicts.

## Parallel Example: User Story 1

```text
Task: "Add risk-only filtering test in src/test/java/com/code/portfolio/project/service/ProjectServiceTest.java"
Task: "Update ProjectController.list to receive optional RiskLevel risk in src/main/java/com/code/portfolio/project/web/ProjectController.java"
```

These are conceptually separable, but implementation should still respect compile order: service signature changes before final build.

## Implementation Strategy

### MVP First

1. Complete setup and foundational checks.
2. Add service tests for risk filtering.
3. Implement service filtering and pagination.
4. Wire the controller parameter.
5. Update README.
6. Run `.\mvnw.cmd verify`.

### Incremental Delivery

This feature has one user story. Deliver it as a single vertical slice from query parameter to tested service behavior and documentation.

## Notes

- Do not create a Flyway migration for risk.
- Do not persist risk in `Project`.
- Keep using existing `RiskCalculator` rules.
- Invalid risk values should use existing enum request binding behavior.
