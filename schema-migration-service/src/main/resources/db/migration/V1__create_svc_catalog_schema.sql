-- Create the svc_catalog schema and base tables
CREATE SCHEMA IF NOT EXISTS svc_catalog AUTHORIZATION madison;

-- Create the collection table, the base document table to which all other tables join.
CREATE TABLE IF NOT EXISTS catalog.document
(
    id          BIGSERIAL PRIMARY KEY,
    fingerprint VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS catalog.document_identifiers
(
    id      BIGSERIAL PRIMARY KEY,
    doc_id  BIGINT NOT NULL REFERENCES catalog.document(id),
    isbn_10 CHAR(10),
    isbn_13 CHAR(13)
);

CREATE TABLE catalog.author
(
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(50),
    middle_name VARCHAR(50),
    last_name   VARCHAR(50),
    suffix      VARCHAR(10),
    full_name   VARCHAR(200) GENERATED ALWAYS AS ( concat(first_name, ' ', middle_name,
                                                          CASE WHEN middle_name IS NOT NULL THEN ' ' ELSE '' END,
                                                          last_name, CASE WHEN suffix IS NOT NULL THEN ', ' ELSE '' END,
                                                          suffix) ) STORED
);