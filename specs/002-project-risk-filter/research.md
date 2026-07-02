# Research: Filter Projects By Risk

## Decision: Keep `risk` as a calculated filter

**Decision**: The risk filter will use the same calculated risk rules already exposed in project responses and reports.

**Rationale**: The project constitution states that risk is calculated dynamically and must not be persisted. Reusing the existing calculation keeps behavior consistent across detail, list, and report responses.

**Alternatives considered**:

- Persist `risk` in the project table: rejected because it violates the current model and would require synchronization when budget or dates change.
- Add a new migration with a generated/calculated column: rejected because the project currently treats risk as domain logic rather than storage logic.

## Decision: Use repository filters for persisted fields and service filtering for risk

**Decision**: Continue using `ProjectSpecifications` for `name`, `status`, and `managerId`. Apply the `risk` filter in `ProjectService`, after retrieving records that match the persisted filters.

**Rationale**: `risk` depends on business rules combining budget and expected duration. Expressing those rules as a portable JPA criteria query would add complexity and could diverge from `RiskCalculator`.

**Alternatives considered**:

- Translate risk rules into JPA predicates: rejected for this feature because date duration calculation is less readable and risks duplicating business rules.
- Filter only the current database page in memory: rejected because it can produce incorrect totals and incomplete pages.

## Decision: Preserve correct pagination metadata

**Decision**: When `risk` is informed, build the paginated result from the full set that matches all filters, then slice according to the requested page and size.

**Rationale**: The spec requires pagination metadata to remain correct. Filtering only after a paged database query would make `totalElements`, `totalPages`, and page content misleading.

**Alternatives considered**:

- Return a normal database page and remove non-matching projects from its content: rejected because metadata becomes wrong.
- Disable pagination when risk is provided: rejected because it changes existing endpoint expectations.

## Decision: Let invalid risk values follow existing enum parameter behavior

**Decision**: Invalid risk values should be rejected by the same request binding behavior used for invalid enum filters.

**Rationale**: The endpoint already uses enum query parameters for status. Using the same pattern makes invalid `risk` handling predictable and avoids custom parsing code.

**Alternatives considered**:

- Parse strings manually in the service: rejected because it moves request validation away from the web boundary.
