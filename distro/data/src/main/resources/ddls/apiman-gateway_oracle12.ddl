
CREATE TABLE apis (org_id VARCHAR2(255) NOT NULL, id VARCHAR2(255) NOT NULL, version VARCHAR2(255) NOT NULL, bean CLOB NOT NULL);
ALTER TABLE apis ADD PRIMARY KEY (org_id, id, version);

CREATE TABLE clients (api_key VARCHAR2(255) NOT NULL, org_id VARCHAR2(255) NOT NULL, id VARCHAR2(255) NOT NULL, version VARCHAR2(255) NOT NULL, bean CLOB NOT NULL);
ALTER TABLE clients ADD PRIMARY KEY (api_key);
ALTER TABLE clients ADD CONSTRAINT UK_clients_1 UNIQUE (org_id, id, version);
CREATE INDEX IDX_clients_1 ON clients(org_id, id, version);

CREATE TABLE dataversion (version NUMBER(38, 0) NOT NULL);

CREATE TABLE requests (
	rstart BIGINT NOT NULL, rend BIGINT NOT NULL, duration BIGINT NOT NULL, 
	month BIGINT NOT NULL, week BIGINT NOT NULL, day BIGINT NOT NULL, hour BIGINT NOT NULL, minute BIGINT NOT NULL,
	api_org_id VARCHAR2(255) NOT NULL, api_id VARCHAR2(255) NOT NULL, api_version VARCHAR2(255) NOT NULL, 
	client_org_id VARCHAR2(255), client_id VARCHAR2(255), client_version VARCHAR2(255), plan VARCHAR2(255),
	user_id VARCHAR2(255), resp_type VARCHAR2(255), bytes_up BIGINT NOT NULL, bytes_down BIGINT NOT NULL
);
CREATE INDEX IDX_requests_1 ON requests(api_org_id, api_id, api_version);
CREATE INDEX IDX_requests_2 ON requests(client_org_id, client_id, client_version);
CREATE INDEX IDX_requests_3 ON requests(resp_type);
