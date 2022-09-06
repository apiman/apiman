
CREATE TABLE gw_apis (org_id VARCHAR(255) NOT NULL, id VARCHAR(255) NOT NULL, version VARCHAR(255) NOT NULL, bean TEXT NOT NULL);
ALTER TABLE gw_apis ADD PRIMARY KEY (org_id, id, version);

CREATE TABLE gw_clients (api_key VARCHAR(255) NOT NULL, org_id VARCHAR(255) NOT NULL, id VARCHAR(255) NOT NULL, version VARCHAR(255) NOT NULL, bean TEXT NOT NULL);
ALTER TABLE gw_clients ADD PRIMARY KEY (api_key);
ALTER TABLE gw_clients ADD CONSTRAINT UK_gw_clients_1 UNIQUE (org_id, id, version);
CREATE INDEX IDX_gw_clients_1 ON gw_clients(org_id, id, version);

CREATE TABLE gw_dataversion (version BIGINT NOT NULL);

CREATE TABLE gw_requests (
	rstart BIGINT NOT NULL, rend BIGINT NOT NULL, duration BIGINT NOT NULL, 
	month BIGINT NOT NULL, week BIGINT NOT NULL, day BIGINT NOT NULL, hour BIGINT NOT NULL, minute BIGINT NOT NULL,
	api_org_id VARCHAR(255) NOT NULL, api_id VARCHAR(255) NOT NULL, api_version VARCHAR(255) NOT NULL, 
	client_org_id VARCHAR(255), client_id VARCHAR(255), client_version VARCHAR(255), plan VARCHAR(255),
	user_id VARCHAR(255), resp_type VARCHAR(255), bytes_up BIGINT NOT NULL, bytes_down BIGINT NOT NULL
);
CREATE INDEX IDX_gw_requests_1 ON gw_requests(api_org_id, api_id, api_version);
CREATE INDEX IDX_gw_requests_2 ON gw_requests(client_org_id, client_id, client_version);
CREATE INDEX IDX_gw_requests_3 ON gw_requests(resp_type);
