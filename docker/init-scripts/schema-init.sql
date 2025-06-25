--The script to initialize the schema was sourced from the Spring Batch Core dependency: org.springframework.batch.core.

CREATE SCHEMA document_schema;
SET SCHEMA 'document_schema';


CREATE TYPE document_status AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TABLE documents (
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
    status document_status DEFAULT 'ACTIVE'
);

CREATE INDEX idx_documents_user ON documents(user_id);
CREATE INDEX idx_documents_name ON documents(document_name);
CREATE INDEX idx_documents_tags ON documents USING GIN (tags);
CREATE INDEX idx_documents_status ON documents(status);

