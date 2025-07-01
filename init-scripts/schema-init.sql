CREATE SCHEMA IF NOT EXISTS document_schema;

SET search_path TO document_schema;


CREATE TABLE IF NOT EXISTS documents (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    tags JSONB,
    minio_path VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    status VARCHAR(255) NOT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_documents_user' AND n.nspname = 'document_schema'
    ) THEN
        CREATE INDEX idx_documents_user ON documents(user_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_documents_name' AND n.nspname = 'document_schema'
    ) THEN
        CREATE INDEX idx_documents_name ON documents(document_name);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'idx_documents_tags' AND n.nspname = 'document_schema'
    ) THEN
        CREATE INDEX idx_documents_tags ON documents USING GIN (tags);
    END IF;
END$$;
