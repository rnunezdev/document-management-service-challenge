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
CREATE INDEX idx_documents_user ON document_schema.documents(user_id);
CREATE INDEX idx_documents_name ON document_schema.documents(document_name);
CREATE INDEX idx_documents_tags ON document_schema.documents USING GIN (tags);