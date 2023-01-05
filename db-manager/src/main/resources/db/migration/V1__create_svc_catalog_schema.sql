-- Create the svc_catalog schema and base tables
CREATE SCHEMA IF NOT EXISTS svc_catalog AUTHORIZATION madison;

-- Create the collection table, the base document table to which all other tables join.
CREATE TABLE svc_catalog.collection(
  id BIGSERIAL,
  ext_id VARCHAR(50) NOT NULL UNIQUE
);