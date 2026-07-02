# Contract: Project List Risk Filter

Endpoint: `GET /api/projects`

Authentication: HTTP Basic, unchanged.

## Query Parameters

Existing parameters remain available:

- `name`
- `status`
- `managerId`
- pagination and sorting parameters

New optional parameter:

| Name | Required | Values | Behavior |
|------|----------|--------|----------|
| `risk` | No | `BAIXO`, `MEDIO`, `ALTO` | Returns only projects whose calculated risk equals the requested value. |

## Examples

List high risk projects:

```http
GET /api/projects?risk=ALTO
```

List high risk projects managed by member 1:

```http
GET /api/projects?managerId=1&risk=ALTO
```

List medium risk projects in a specific status:

```http
GET /api/projects?status=EM_ANDAMENTO&risk=MEDIO
```

## Response

The response keeps the existing paginated shape. Each project summary already includes `risk`.

```json
{
  "content": [
    {
      "id": 10,
      "name": "Projeto Critico",
      "status": "EM_ANDAMENTO",
      "risk": "ALTO",
      "totalBudget": 750000.0,
      "startDate": "2026-01-01",
      "expectedEndDate": "2026-08-01",
      "managerId": 1
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

## Error Behavior

Invalid values such as `risk=INVALIDO` are rejected consistently with invalid enum query parameters already used by the API.
