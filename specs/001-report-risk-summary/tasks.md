# Tasks: Portfolio Report Risk Summary

**Input**: `specs/001-report-risk-summary/spec.md` and `specs/001-report-risk-summary/plan.md`

## Phase 1: Contract

- [x] T001 Define the expected portfolio report response change in `specs/001-report-risk-summary/contracts/portfolio-report-risk-summary.md`

## Phase 2: Tests

- [x] T002 Add unit coverage for `quantidadePorRisco` in `src/test/java/com/code/portfolio/project/service/PortfolioReportServiceTest.java`
- [x] T003 Ensure existing report tests still assert previous fields

## Phase 3: Implementation

- [x] T004 Add `quantidadePorRisco` to `src/main/java/com/code/portfolio/project/dto/PortfolioReportResponse.java`
- [x] T005 Inject and use `RiskCalculator` in `src/main/java/com/code/portfolio/project/service/PortfolioReportService.java`
- [x] T006 Aggregate current projects into `Map<RiskLevel, Long>`

## Phase 4: Documentation & Verification

- [x] T007 Update `README.md` report documentation/sample to mention `quantidadePorRisco`
- [x] T008 Run `.\mvnw.cmd verify`
