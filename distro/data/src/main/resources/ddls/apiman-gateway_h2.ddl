
CREATE TABLE apis (org_id VARCHAR(255) NOT NULL, id VARCHAR(255) NOT NULL, version VARCHAR(255) NOT NULL, bean CLOB NOT NULL);
ALTER TABLE apis ADD PRIMARY KEY (org_id, id, version);

CREATE TABLE clients (api_key VARCHAR(255) NOT NULL, org_id VARCHAR(255) NOT NULL, id VARCHAR(255) NOT NULL, version VARCHAR(255) NOT NULL, bean CLOB NOT NULL);
ALTER TABLE clients ADD PRIMARY KEY (api_key);
ALTER TABLE clients ADD CONSTRAINT UK_clients_1 UNIQUE (org_id, id, version);
CREATE INDEX IDX_clients_1 ON clients(org_id, id, version);

CREATE TABLE dataversion (version BIGINT NOT NULL);
