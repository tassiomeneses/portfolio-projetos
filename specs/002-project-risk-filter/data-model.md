# Data Model: Filter Projects By Risk

## Project

Represents an existing portfolio project.

Relevant fields for this feature:

- `name`: used by the existing name filter.
- `status`: used by the existing status filter.
- `managerId`: used by the existing manager filter.
- `totalBudget`: used to calculate risk.
- `startDate`: used to calculate risk.
- `expectedEndDate`: used to calculate risk.

Relationships:

- Project can have allocated member IDs through existing project member records.

Validation rules:

- Existing date and budget validation continue to apply when projects are created or updated.
- No new project field is introduced.

## Risk Level

Represents a calculated classification for a project.

Allowed values:

- `BAIXO`
- `MEDIO`
- `ALTO`

Validation rules:

- A list request can filter by exactly one risk level.
- Invalid values are rejected by the request boundary.
- Risk is not stored and is recalculated from project data.

## Project List Result

Represents a paginated list of project summaries.

Relevant fields:

- Page content: only projects matching every informed filter.
- Total elements: total number of projects matching every informed filter.
- Page size and number: unchanged from the existing list behavior.

Validation rules:

- Omitted `risk` keeps existing list semantics.
- Informed `risk` intersects with `name`, `status`, and `managerId`.
