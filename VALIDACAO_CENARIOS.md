# Validação de Cenários

Data da validação: 2026-06-14  
Ambiente: Java 25, Docker Desktop e PowerShell  

## Objetivo

Validar o fluxo local padrão do projeto:

1. Executar a aplicação com `spring-boot:run`.
2. Deixar o Spring Boot subir o PostgreSQL pelo `docker-compose.yml`.
3. Usar a porta padrão do banco: `localhost:5432`.
4. Confirmar migrations Flyway, segurança, endpoints principais e regras de negócio.

## Configuração Validada

O comando abaixo mostra que, sem profile, o Docker Compose expõe apenas o serviço de banco para o fluxo local:

```powershell
docker compose config --services
```

Resultado:

```text
postgres
```

Com o profile `app`, o stack completo continua disponível para execução 100% Docker:

```powershell
docker compose --profile app config --services
```

Resultado:

```text
postgres
app
```

## Build e Testes Automatizados

Comando executado:

```powershell
.\mvnw.cmd verify
```

Resultado:

```text
BUILD SUCCESS
Tests run: 47, Failures: 0, Errors: 0, Skipped: 0
All coverage checks have been met.
```

Resumo por suíte:

| Suíte | Testes | Resultado |
|---|---:|---|
| `ProjectStatusTest` | 12 | PASS |
| `RiskCalculatorTest` | 12 | PASS |
| `PortfolioReportServiceTest` | 2 | PASS |
| `ProjectServiceTest` | 21 | PASS |

## Boot Local com Banco Automático

Comando executado:

```powershell
.\mvnw.cmd spring-boot:run
```

Evidências do log:

```text
Using Docker Compose file docker-compose.yml
Container portfolio-postgres Healthy
Database: jdbc:postgresql://127.0.0.1:5432/portfolio?ApplicationName=portfolio-projetos (PostgreSQL 17.10)
Successfully validated 1 migration
Schema "public" is up to date. No migration necessary.
Tomcat started on port 8080 (http)
Started PortfolioProjetosApplication
```

Estado do container:

```powershell
docker ps --filter name=portfolio --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

Resultado:

```text
NAMES                STATUS                   PORTS
portfolio-postgres   Up 3 minutes (healthy)   0.0.0.0:5432->5432/tcp
```

Evidência Flyway no banco:

```powershell
docker exec portfolio-postgres psql -U portfolio -d portfolio -c "select version, success from flyway_schema_history order by installed_rank;"
```

Resultado:

```text
 version | success
---------+---------
 1       | t
```

Tabelas criadas:

```text
flyway_schema_history
project
project_member
```

## Cenários HTTP Validados

Base URL: `http://localhost:8080`  
Autenticação da API protegida: HTTP Basic `admin/admin123`

| Cenário | Método | Rota | Esperado | Obtido | Resultado |
|---|---|---|---:|---:|---|
| Health público | GET | `/actuator/health` | 200 | 200 | PASS |
| Projetos exige autenticação | GET | `/api/projects` | 401 | 401 | PASS |
| Lista membros do provider público | GET | `/api/members-provider/members` | 200 | 200 | PASS |
| Cria funcionário no provider público | POST | `/api/members-provider/members` | 201 | 201 | PASS |
| Cria projeto autenticado | POST | `/api/projects` | 201 | 201 | PASS |
| Consulta projeto criado | GET | `/api/projects/{id}` | 200 | 200 | PASS |
| Lista projeto por filtro de nome | GET | `/api/projects?name=...` | 200 | 200 | PASS |
| Bloqueia alocação de estagiário | POST | `/api/projects/{id}/members` | 422 | 422 | PASS |
| Aloca funcionário no projeto | POST | `/api/projects/{id}/members` | 200 | 200 | PASS |
| Bloqueia pulo de status | PATCH | `/api/projects/{id}/status` | 422 | 422 | PASS |
| Transição para `ANALISE_REALIZADA` | PATCH | `/api/projects/{id}/status` | 200 | 200 | PASS |
| Transição para `ANALISE_APROVADA` | PATCH | `/api/projects/{id}/status` | 200 | 200 | PASS |
| Transição para `INICIADO` | PATCH | `/api/projects/{id}/status` | 200 | 200 | PASS |
| Transição para `PLANEJADO` | PATCH | `/api/projects/{id}/status` | 200 | 200 | PASS |
| Transição para `EM_ANDAMENTO` | PATCH | `/api/projects/{id}/status` | 200 | 200 | PASS |
| Bloqueia exclusão de projeto em andamento | DELETE | `/api/projects/{id}` | 422 | 422 | PASS |
| Relatório de portfólio autenticado | GET | `/api/reports/portfolio` | 200 | 200 | PASS |
| Cria projeto para validar delete | POST | `/api/projects` | 201 | 201 | PASS |
| Exclui projeto em análise | DELETE | `/api/projects/{id}` | 204 | 204 | PASS |
| Confirma projeto excluído | GET | `/api/projects/{id}` | 404 | 404 | PASS |

## Amostras de Resposta

Health:

```json
{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

Funcionário criado no provider:

```json
{
  "id": 7,
  "name": "Funcionario Integracao",
  "attribution": "funcionário"
}
```

Projeto criado:

```json
{
  "id": 4,
  "name": "Projeto Integracao",
  "startDate": "2026-01-01",
  "expectedEndDate": "2026-05-01",
  "actualEndDate": null,
  "totalBudget": 150000.0,
  "managerId": 1,
  "status": "EM_ANALISE",
  "risk": "MEDIO"
}
```

Relatório de portfólio:

```json
{
  "quantidadePorStatus": {
    "EM_ANALISE": 1,
    "INICIADO": 1,
    "EM_ANDAMENTO": 1
  },
  "totalOrcadoPorStatus": {
    "EM_ANALISE": 150000.0,
    "INICIADO": 750000.0,
    "EM_ANDAMENTO": 150000.0
  },
  "mediaDuracaoDiasProjetosEncerrados": null,
  "totalMembrosUnicosAlocados": 3
}
```

Contagem no banco após os cenários:

```text
select count(*) as total_project from project;

 total_project
---------------
             3

select count(*) as total_project_member from project_member;

 total_project_member
----------------------
                    3
```

## Conclusão

O fluxo local está validado: ao executar `spring-boot:run`, o Spring Boot sobe o PostgreSQL via Docker Compose na porta padrão `5432`, espera o container ficar saudável, conecta no banco, valida o schema com Flyway e disponibiliza a API em `8080`.

Os cenários principais de integração passaram: autenticação, provider mockado de membros, criação/consulta/listagem de projetos, alocação de funcionário, bloqueio de estagiário, máquina de estados, bloqueio de exclusão em status protegido, exclusão em status permitido e relatório do portfólio.
