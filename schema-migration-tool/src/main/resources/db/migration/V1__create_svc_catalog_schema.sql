-- Create the svc_catalog schema and base tables
CREATE SCHEMA IF NOT EXISTS catalog AUTHORIZATION madison;


-- Create the collection table, the base document table to which all other tables join.
CREATE TABLE IF NOT EXISTS catalog.document
(
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(100),
    page_count   INTEGER,
    fingerprint  VARCHAR(50) NOT NULL UNIQUE,
    content_type VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS catalog.identifiers
(
    id      BIGSERIAL PRIMARY KEY,
    doc_id  BIGINT NOT NULL REFERENCES catalog.document (id),
    isbn_10 CHAR(10),
    isbn_13 CHAR(13)
);

CREATE INDEX IF NOT EXISTS identifiers_fk_doc_id ON catalog.identifiers (doc_id);

CREATE TABLE catalog.author
(
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50),
    last_name   VARCHAR(50) NOT NULL,
    suffix      VARCHAR(10),
    full_name   VARCHAR(200) GENERATED ALWAYS AS ( first_name || ' ' ||
                                                   CASE WHEN middle_name IS NOT NULL THEN ' ' ELSE '' END ||
                                                   last_name ||
                                                   CASE
                                                       WHEN suffix IS NOT NULL THEN ', ' || author.suffix
                                                       ELSE '' END) STORED
);

CREATE TABLE catalog.author_document
(
    author_id   BIGINT NOT NULL REFERENCES catalog.author (id),
    document_id BIGINT NOT NULL REFERENCES catalog.document (id),
    PRIMARY KEY (author_id, document_id)
);