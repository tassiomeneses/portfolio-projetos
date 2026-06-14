-- Esquema inicial do sistema de gestao de portfolio de projetos.
-- Membros sao gerenciados por uma API externa (mockada), portanto nao ha tabela de membros:
-- guardamos apenas as referencias (manager_id / member_id).

create table project (
    id                bigint generated always as identity primary key,
    name              varchar(150)   not null,
    start_date        date           not null,
    expected_end_date date           not null,
    actual_end_date   date,
    total_budget      numeric(15, 2) not null,
    description       text,
    manager_id        bigint         not null,
    status            varchar(30)    not null,
    created_at        timestamp with time zone not null,
    updated_at        timestamp with time zone not null
);

create table project_member (
    id           bigint generated always as identity primary key,
    project_id   bigint not null references project (id) on delete cascade,
    member_id    bigint not null,
    allocated_at timestamp with time zone not null,
    constraint uk_project_member unique (project_id, member_id)
);

create index idx_project_status on project (status);
create index idx_project_manager on project (manager_id);
create index idx_project_member_member on project_member (member_id);
