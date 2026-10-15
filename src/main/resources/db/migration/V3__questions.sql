create table questions (
    id uuid primary key,
    user_id uuid not null references users (id),
    workspace_id uuid not null references workspaces (id),
    question text not null,
    answer text not null,
    abstained boolean not null,
    created_at timestamptz not null default now()
);

create table question_citations (
    id uuid primary key,
    question_id uuid not null references questions (id),
    workspace_id uuid not null references workspaces (id),
    chunk_id varchar(64) not null,
    document_name varchar(500) not null,
    heading varchar(500) not null
);
