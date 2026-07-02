# Contract: Portfolio Report Risk Summary

Endpoint: `GET /api/reports/portfolio`

Authentication: HTTP Basic, unchanged.

## Response Change

The response adds `quantidadePorRisco`.

```json
{
  "quantidadePorStatus": {
    "EM_ANALISE": 2
  },
  "totalOrcadoPorStatus": {
    "EM_ANALISE": 150000.0
  },
  "quantidadePorRisco": {
    "BAIXO": 1,
    "MEDIO": 1,
    "ALTO": 1
  },
  "mediaDuracaoDiasProjetosEncerrados": null,
  "totalMembrosUnicosAlocados": 3
}
```

Risk values use the existing `RiskLevel` enum: `BAIXO`, `MEDIO`, `ALTO`.
