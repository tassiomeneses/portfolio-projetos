# Portfólio de Projetos API

API REST para gerenciar o portfólio de projetos de uma empresa, cobrindo cadastro,
andamento, equipe, orçamento, risco e relatórios.

## O Que Tem

- CRUD de projetos com paginação e filtros.
- Máquina de estados para controlar o ciclo de vida do projeto.
- Classificação automática de risco por orçamento e prazo.
- Alocação de membros com regras de negócio.
- API mockada de membros, simulando um serviço externo.
- Relatório consolidado do portfólio.
- Segurança com HTTP Basic.
- Swagger UI e coleção Postman.
- Banco PostgreSQL em Docker, iniciado automaticamente no boot local.

## Tecnologias

- Java 25
- Spring Boot 4.0.7
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- PostgreSQL 17
- Flyway
- springdoc-openapi
- JUnit 5, Mockito e JaCoCo
- Docker Compose
- Maven Wrapper

## Pré-Requisitos

- JDK 25 configurado no `JAVA_HOME`.
- Docker com Docker Compose.

Não é necessário instalar Maven, porque o projeto usa Maven Wrapper (`mvnw`).

## Como Rodar

Com o Docker aberto, execute:

```bash
# Linux/macOS
./mvnw spring-boot:run
```

```powershell
# Windows
.\mvnw.cmd spring-boot:run
```

Esse comando:

1. Sobe o PostgreSQL pelo `docker-compose.yml`.
2. Aguarda o banco ficar saudável.
3. Executa as migrations do Flyway.
4. Sobe a API em `http://localhost:8080`.

URLs úteis:

| Recurso | URL |
|---|---|
| API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| Health | `http://localhost:8080/actuator/health` |
| PostgreSQL | `localhost:5432` |

## Rodar Tudo Com Docker Compose

Se quiser subir API e banco em containers:

```bash
docker compose --profile app up --build
```

## Credenciais

A API usa HTTP Basic.

| Usuário | Senha |
|---|---|
| `admin` | `admin123` |

Endpoints públicos:

- `/actuator/health`
- `/swagger-ui.html`
- `/v3/api-docs`
- `/api/members-provider/**`

Todos os demais endpoints exigem autenticação.

## Configuração

Valores padrão:

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `portfolio` | Nome do banco |
| `DB_USER` | `portfolio` | Usuário do banco |
| `DB_PASSWORD` | `portfolio` | Senha do banco |
| `SERVER_PORT` | `8080` | Porta da API |
| `APP_USER` | `admin` | Usuário da API |
| `APP_PASSWORD` | `admin123` | Senha da API |
| `MEMBERS_API_BASE_URL` | `http://localhost:8080/api/members-provider` | URL da API de membros |
| `POSTGRES_PORT` | `5432` | Porta exposta pelo container do PostgreSQL |

## Endpoints Principais

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/projects` | Cria projeto |
| `GET` | `/api/projects` | Lista projetos com paginação e filtros |
| `GET` | `/api/projects/{id}` | Detalha um projeto |
| `PUT` | `/api/projects/{id}` | Atualiza um projeto |
| `DELETE` | `/api/projects/{id}` | Exclui um projeto |
| `PATCH` | `/api/projects/{id}/status` | Altera o status do projeto |
| `POST` | `/api/projects/{id}/members` | Aloca membros no projeto |
| `DELETE` | `/api/projects/{id}/members/{memberId}` | Remove membro do projeto |
| `GET` | `/api/reports/portfolio` | Gera relatório do portfólio |
| `GET` | `/api/members-provider/members` | Lista membros da API mockada |
| `POST` | `/api/members-provider/members` | Cria membro na API mockada |

## Postman

A pasta [`collections/`](collections/) contém:

- `portfolio-projetos.postman_collection.json`
- `portfolio-projetos.postman_environment.json`

Importe os dois arquivos no Postman. A coleção já usa as credenciais padrão
`admin/admin123`.

Fluxo sugerido:

1. Criar ou listar membros na API mockada.
2. Criar projeto.
3. Alocar membros.
4. Alterar status do projeto.
5. Consultar o relatório do portfólio.

## Regras de Negócio

- Todo projeto inicia em `EM_ANALISE`.
- A sequência de status é:
  `EM_ANALISE -> ANALISE_REALIZADA -> ANALISE_APROVADA -> INICIADO -> PLANEJADO -> EM_ANDAMENTO -> ENCERRADO`.
- `CANCELADO` pode ser aplicado a qualquer status não final.
- Não é permitido pular etapas da máquina de estados.
- Projetos em `INICIADO`, `EM_ANDAMENTO` ou `ENCERRADO` não podem ser excluídos.
- Para iniciar um projeto, ele precisa ter ao menos 1 membro alocado.
- Um projeto pode ter no máximo 10 membros.
- Apenas membros com atribuição `funcionário` podem ser alocados.
- Um membro pode estar em no máximo 3 projetos ativos.
- Membros não são persistidos no banco da aplicação; eles vêm da API mockada.

## Risco do Projeto

O risco é calculado automaticamente e não é persistido.

| Risco | Critério |
|---|---|
| `BAIXO` | Orçamento até R$ 100.000 e prazo até 3 meses |
| `MEDIO` | Orçamento entre R$ 100.001 e R$ 500.000 ou prazo entre 3 e 6 meses |
| `ALTO` | Orçamento acima de R$ 500.000 ou prazo acima de 6 meses |

## Banco de Dados

O schema é versionado com Flyway em [`src/main/resources/db/migration`](src/main/resources/db/migration).

O Hibernate roda com `ddl-auto=validate`, então ele valida o schema, mas não cria nem
altera tabelas automaticamente. Alterações de banco devem ser feitas por novas
migrations.

## Testes

Execute:

```bash
./mvnw verify
```

No Windows:

```powershell
.\mvnw.cmd verify
```

Esse comando roda os testes e valida a cobertura mínima configurada no JaCoCo.

O relatório de cobertura é gerado em:

```text
target/site/jacoco/index.html
```

## Problemas Comuns

- Se a porta `5432` estiver ocupada, pare o PostgreSQL local ou outro container que
  esteja usando essa porta.
- Se a porta `8080` estiver ocupada, defina outra porta com `SERVER_PORT`.
