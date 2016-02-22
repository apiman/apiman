
CREATE TABLE apis (org_id VARCHAR2(255) NOT NULL, id VARCHAR2(255) NOT NULL, version VARCHAR2(255) NOT NULL, bean CLOB NOT NULL);
ALTER TABLE apis ADD PRIMARY KEY (org_id, id, version);

CREATE TABLE clients (org_id VARCHAR2(255) NOT NULL, id VARCHAR2(255) NOT NULL, version VARCHAR2(255) NOT NULL, bean CLOB NOT NULL);
ALTER TABLE clients ADD PRIMARY KEY (org_id, id, version);

CREATE TABLE contracts (api_key VARCHAR2(255) NOT NULL, client_org_id VARCHAR2(255) NOT NULL, client_id VARCHAR2(255) NOT NULL, client_version VARCHAR2(255) NOT NULL, bean CLOB NOT NULL);
ALTER TABLE contracts ADD PRIMARY KEY (api_key);
ALTER TABLE contracts ADD CONSTRAINT FK_contracts_1 FOREIGN KEY (client_org_id, client_id, client_version) REFERENCES clients (org_id, id, version);
CREATE INDEX IDX_contracts_1 ON contracts(client_org_id, client_id, client_version);
