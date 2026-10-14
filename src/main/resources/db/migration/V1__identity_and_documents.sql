create table users (
    id uuid primary key,
    email varchar(320) not null unique,
    password_hash varchar(255) not null,
    created_at timestamptz not null default now()
);

create table workspaces (
    id uuid primary key,
    name varchar(200) not null,
    owner_user_id uuid not null references users (id),
    created_at timestamptz not null default now()
);

create table documents (
    id uuid primary key,
    workspace_id uuid not null references workspaces (id),
    original_name varchar(500) not null,
    status varchar(32) not null,
    created_at timestamptz not null default now(),
    constraint documents_status_check check (status in ('TEXT_STORED', 'FAILED'))
);

create table document_chunks (
    id uuid primary key,
    document_id uuid not null references documents (id),
    workspace_id uuid not null references workspaces (id),
    chunk_index integer not null,
    heading varchar(500) not null,
    body text not null,
    constraint document_chunks_document_index unique (document_id, chunk_index)
);
