-- Initialize pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- The application uses JPA ddl-auto=update to create tables,
-- but the vector extension must exist before any vector columns can be created.
