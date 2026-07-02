# Quickstart: Validate Project Risk Filter

## Prerequisites

- JDK 25 configured.
- Docker available if running the API locally with PostgreSQL.
- Default API credentials: `admin/admin123`.

## Automated Validation

Run the project verification command:

```powershell
.\mvnw.cmd verify
```

Expected outcome:

- Build succeeds.
- Project service tests cover risk-only filtering, combined filtering, omitted risk behavior, and invalid risk handling where applicable.
- Existing coverage checks pass.

## Manual API Validation

Start the API:

```powershell
.\mvnw.cmd spring-boot:run
```

Create or reuse projects that produce each risk level:

- `BAIXO`: budget up to 100000 and expected duration up to 3 months.
- `MEDIO`: budget from 100001 to 500000 or expected duration from 3 to 6 months.
- `ALTO`: budget above 500000 or expected duration above 6 months.

Validate high risk filtering:

```powershell
curl.exe -u admin:admin123 "http://localhost:8080/api/projects?risk=ALTO"
```

Expected outcome:

- Every item in `content` has `"risk": "ALTO"`.
- Pagination metadata reflects only matching projects.

Validate combined filters:

```powershell
curl.exe -u admin:admin123 "http://localhost:8080/api/projects?managerId=1&risk=ALTO"
```

Expected outcome:

- Every item has `"managerId": 1`.
- Every item has `"risk": "ALTO"`.

Validate omitted risk behavior:

```powershell
curl.exe -u admin:admin123 "http://localhost:8080/api/projects"
```

Expected outcome:

- The list behaves as before and is not restricted by risk.

Validate invalid risk rejection:

```powershell
curl.exe -u admin:admin123 "http://localhost:8080/api/projects?risk=INVALIDO"
```

Expected outcome:

- The API rejects the request instead of returning an unfiltered list.
