create extension if not exists vector;

alter table documents
    add column failure_message text;

alter table documents
    drop constraint documents_status_check;

alter table documents
    add constraint documents_status_check
    check (status in ('TEXT_STORED', 'COMPLETED', 'FAILED'));

alter table document_chunks
    add column embedding vector(1024);
