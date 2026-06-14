# Portfólio de Projetos — API

API REST para gerenciar o portfólio de projetos de uma empresa, cobrindo o ciclo de
vida completo de cada projeto — da análise de viabilidade à finalização — com gestão
de equipe, orçamento e classificação de risco.

## Tecnologias

- **Java 25**
- **Spring Boot 4.0.7** (Spring Framework 7, Spring Security 7, Spring Data JPA / Hibernate 7)
- **PostgreSQL 17** (em Docker)
- **Flyway** para versionamento do banco
- **springdoc-openapi 3** (Swagger UI)
- **JUnit 5 + Mockito + JaCoCo** (testes e cobertura)
- **Maven** com wrapper (`mvnw`) — não é preciso instalar o Maven

## Arquitetura

Arquitetura em camadas (MVC), organizada **por funcionalidade**, com separação clara
entre controller, service e repository:

```
com.code.portfolio
├── config                 # Security, OpenAPI, RestClient
├── common/exception       # Exceções de domínio + tratamento global (RFC 7807)
├── members                # Cliente (RestClient) da API externa de membros
├── membersprovider        # API externa de membros MOCKADA (serviço de terceiros simulado)
└── project
    ├── domain             # Entidades, enum de status (máquina de estados), cálculo de risco
    ├── dto                # Records de entrada/saída
    ├── mapper             # Conversão entidade <-> DTO
    ├── repository         # Spring Data JPA + Specifications (filtros)
    ├── service            # Regras de negócio (ProjectService, PortfolioReportService)
    └── web                # Controllers REST
```

## Pré-requisitos

- **JDK 25** (`JAVA_HOME` apontando para um JDK 25)
- **Docker** + **Docker Compose**

## Como executar

```bash
# 1. Subir o banco PostgreSQL
docker compose up -d

# 2. Rodar a aplicação (usa o wrapper; não precisa ter Maven instalado)
./mvnw spring-boot:run          # Linux/macOS
.\mvnw.cmd spring-boot:run      # Windows
```

A aplicação sobe em **http://localhost:8080**.

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **Health:** http://localhost:8080/actuator/health

Para empacotar e rodar o `jar`:

```bash
./mvnw clean package
java -jar target/portfolio-projetos-0.0.1-SNAPSHOT.jar
```

## Configuração

Tudo tem valores padrão; sobrescreva por variáveis de ambiente quando necessário:

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_HOST` / `DB_PORT` | `localhost` / `5432` | Host/porta do PostgreSQL |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `portfolio` | Banco e credenciais |
| `SERVER_PORT` | `8080` | Porta da aplicação |
| `APP_USER` / `APP_PASSWORD` | `admin` / `admin123` | Credenciais da API |
| `MEMBERS_API_BASE_URL` | `http://localhost:8080/api/members-provider` | URL da API de membros |
| `POSTGRES_PORT` | `5432` | Porta exposta pelo container (docker-compose) |

## Segurança

Segurança básica com **HTTP Basic** e usuário **em memória** (senha protegida com BCrypt).
Credenciais padrão: **`admin` / `admin123`**.

Endpoints públicos (sem autenticação): Swagger, `/actuator/health` e a **API externa de
membros** (`/api/members-provider/**`), que representa um serviço de terceiros. Todo o
restante exige autenticação.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/projects` | Cria projeto (inicia em `EM_ANALISE`) |
| `GET` | `/api/projects` | Lista com paginação e filtros (`name`, `status`, `managerId`) |
| `GET` | `/api/projects/{id}` | Detalha um projeto |
| `PUT` | `/api/projects/{id}` | Atualiza um projeto |
| `DELETE` | `/api/projects/{id}` | Exclui (bloqueado em `INICIADO`/`EM_ANDAMENTO`/`ENCERRADO`) |
| `PATCH` | `/api/projects/{id}/status` | Altera o status (respeita a sequência) |
| `POST` | `/api/projects/{id}/members` | Aloca membros |
| `DELETE` | `/api/projects/{id}/members/{memberId}` | Remove alocação |
| `GET` | `/api/reports/portfolio` | Relatório resumido do portfólio |
| `POST`/`GET` | `/api/members-provider/members` | API externa mockada de membros |

## Coleção Postman

A pasta [`collections/`](collections/) contém:

- `portfolio-projetos.postman_collection.json` — todos os endpoints, com exemplos de
  corpo, variáveis e scripts que encadeiam o `id` criado.
- `portfolio-projetos.postman_environment.json` — ambiente local.

Importe os dois no Postman. A autenticação Basic já vem configurada na coleção
(`{{username}}`/`{{password}}`).

## Regras de negócio

- **CRUD de projetos** com: nome, data de início, previsão de término, data real de
  término, orçamento (`BigDecimal`), descrição, gerente e status.
- **Classificação de risco** (calculada dinamicamente, não persistida) — assume o maior
  risco entre os critérios de orçamento e prazo:
  - Baixo: orçamento ≤ R$ 100.000 **e** prazo ≤ 3 meses
  - Médio: orçamento entre R$ 100.001 e R$ 500.000 **ou** prazo de 3 a 6 meses
  - Alto: orçamento > R$ 500.000 **ou** prazo > 6 meses
- **Máquina de estados** dos status (fixos):
  `EM_ANALISE → ANALISE_REALIZADA → ANALISE_APROVADA → INICIADO → PLANEJADO → EM_ANDAMENTO → ENCERRADO`,
  com `CANCELADO` aplicável a qualquer status não final. Não é permitido pular etapas.
- **Exclusão** bloqueada quando o status é `INICIADO`, `EM_ANDAMENTO` ou `ENCERRADO`.
- **Membros** não são cadastrados diretamente: são criados/consultados por uma **API REST
  externa mockada**, consumida pelo domínio via `RestClient`.
- **Alocação**: apenas membros com atribuição `funcionário`; de 1 a 10 por projeto; um
  membro não pode estar em mais de 3 projetos simultâneos com status diferente de
  `ENCERRADO`/`CANCELADO`.
- **Relatório do portfólio**: quantidade de projetos por status, total orçado por status,
  média de duração dos projetos encerrados e total de membros únicos alocados.

## Decisões de projeto e premissas

- **API de membros mockada**: implementada como um provedor embutido com armazenamento
  em memória (`/api/members-provider`) e alguns membros pré-carregados (id 1 = gerente;
  ids 2–4 = funcionários; id 5 = estagiário). O domínio nunca acessa membros pelo banco —
  sempre via `RestClient`, com a URL externalizada (`MEMBERS_API_BASE_URL`). Como é um
  serviço de terceiros simulado, fica fora da autenticação da aplicação.
- **Prazo do risco**: medido em meses completos (`ChronoUnit.MONTHS`) entre início e
  previsão de término.
- **"Mínimo de 1 membro"**: interpretado como pré-condição para **iniciar** um projeto
  (transição para `INICIADO` exige ao menos 1 membro alocado). O máximo de 10 é validado
  em cada alocação.
- **Data real de término**: preenchida automaticamente ao encerrar, quando não informada.
- **Validação**: campos via Bean Validation; regras entre campos e de negócio na camada
  de serviço, com respostas padronizadas em **RFC 7807 (`ProblemDetail`)**.
- **DDL**: o schema é versionado por **Flyway**; o Hibernate roda em `ddl-auto=validate`.

## Testes e cobertura

```bash
./mvnw verify
```

Executa os testes unitários e gera o relatório de cobertura em
`target/site/jacoco/index.html`. A verificação de cobertura (JaCoCo) impõe **mínimo de
70%** sobre as **regras de negócio** (pacotes de domínio e serviço); camadas triviais e
de infraestrutura (DTOs, mappers, controllers, configuração, repositórios) são excluídas
da métrica. Foco dos testes: cálculo de risco, máquina de estados, regras do
`ProjectService` e do `PortfolioReportService`.

## Solução de problemas

- **Porta 5432 já em uso** (outro PostgreSQL local): suba o container em outra porta e
  aponte a aplicação para ela:
  ```bash
  POSTGRES_PORT=5433 docker compose up -d
  DB_PORT=5433 ./mvnw spring-boot:run
  ```
- **Falha ao baixar dependências** (ambientes com mirror/repositório Maven corporativo
  que não exponha o Maven Central): use um `settings.xml` apontando para o Central, por
  exemplo `./mvnw -s settings-central.xml ...`.
