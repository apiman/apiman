
CREATE TABLE gw_apis (org_id VARCHAR2(255) NOT NULL, id VARCHAR2(255) NOT NULL, version VARCHAR2(255) NOT NULL, bean CLOB NOT NULL);
ALTER TABLE gw_apis ADD PRIMARY KEY (org_id, id, version);

CREATE TABLE gw_clients (api_key VARCHAR2(255) NOT NULL, org_id VARCHAR2(255) NOT NULL, id VARCHAR2(255) NOT NULL, version VARCHAR2(255) NOT NULL, bean CLOB NOT NULL);
ALTER TABLE gw_clients ADD PRIMARY KEY (api_key);
ALTER TABLE gw_clients ADD CONSTRAINT UK_gw_clients_1 UNIQUE (org_id, id, version);
CREATE INDEX IDX_gw_clients_1 ON gw_clients(org_id, id, version);

CREATE TABLE gw_dataversion (version NUMBER(38, 0) NOT NULL);

CREATE TABLE gw_requests (
	rstart BIGINT NOT NULL, rend BIGINT NOT NULL, duration BIGINT NOT NULL, 
	month BIGINT NOT NULL, week BIGINT NOT NULL, day BIGINT NOT NULL, hour BIGINT NOT NULL, minute BIGINT NOT NULL,
	api_org_id VARCHAR2(255) NOT NULL, api_id VARCHAR2(255) NOT NULL, api_version VARCHAR2(255) NOT NULL, 
	client_org_id VARCHAR2(255), client_id VARCHAR2(255), client_version VARCHAR2(255), plan VARCHAR2(255),
	user_id VARCHAR2(255), resp_type VARCHAR2(255), bytes_up BIGINT NOT NULL, bytes_down BIGINT NOT NULL
);
CREATE INDEX IDX_gw_requests_1 ON gw_requests(api_org_id, api_id, api_version);
CREATE INDEX IDX_gw_requests_2 ON gw_requests(client_org_id, client_id, client_version);
CREATE INDEX IDX_gw_requests_3 ON gw_requests(resp_type);
