-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: /Users/msavy/oss/apiman/apiman/distro/ddl/src/main/liquibase/master.xml
-- Ran at: 21/10/2021, 12:57
-- Against: apiman@offline:oracle?version=19&caseSensitive=true&changeLogFile=/Users/msavy/oss/apiman/apiman/distro/ddl/target/changelog/oracle/databasechangelog.csv
-- Liquibase version: 4.4.3
-- *********************************************************************

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-1::apiman (generated)
CREATE TABLE client_versions (id NUMBER(38, 0) NOT NULL, created_by VARCHAR2(255) NOT NULL, created_on TIMESTAMP NOT NULL, modified_by VARCHAR2(255) NOT NULL, modified_on TIMESTAMP NOT NULL, published_on TIMESTAMP, retired_on TIMESTAMP, status VARCHAR2(255) NOT NULL, version VARCHAR2(255) NOT NULL, client_id VARCHAR2(255), client_org_id VARCHAR2(255), apikey VARCHAR2(255) NOT NULL);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-2::apiman (generated)
CREATE TABLE clients (id VARCHAR2(255) NOT NULL, created_by VARCHAR2(255) NOT NULL, created_on TIMESTAMP NOT NULL, description VARCHAR2(512), name VARCHAR2(255) NOT NULL, organization_id VARCHAR2(255) NOT NULL);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-3::apiman (generated)
CREATE TABLE auditlog (id NUMBER(38, 0) NOT NULL, created_on TIMESTAMP NOT NULL, data CLOB, entity_id VARCHAR2(255), entity_type VARCHAR2(255) NOT NULL, entity_version VARCHAR2(255), organization_id VARCHAR2(255) NOT NULL, what VARCHAR2(255) NOT NULL, who VARCHAR2(255) NOT NULL);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-4::apiman (generated)
CREATE TABLE contracts (id NUMBER(38, 0) NOT NULL, created_by VARCHAR2(255) NOT NULL, created_on TIMESTAMP NOT NULL, clientv_id NUMBER(38, 0), planv_id NUMBER(38, 0), apiv_id NUMBER(38, 0));

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-5::apiman (generated)
CREATE TABLE endpoint_properties (api_version_id NUMBER(38, 0) NOT NULL, value VARCHAR2(255), name VARCHAR2(255) NOT NULL);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-6::apiman (generated)
CREATE TABLE gateways (id VARCHAR2(255) NOT NULL, configuration CLOB NOT NULL, created_by VARCHAR2(255) NOT NULL, created_on TIMESTAMP NOT NULL, description VARCHAR2(512), modified_by VARCHAR2(255) NOT NULL, modified_on TIMESTAMP NOT NULL, name VARCHAR2(255) NOT NULL, type VARCHAR2(255) NOT NULL);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-7::apiman (generated)
CREATE TABLE memberships (id NUMBER(38, 0) NOT NULL, created_on TIMESTAMP, org_id VARCHAR2(255), role_id VARCHAR2(255), user_id VARCHAR2(255));

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-8::apiman (generated)
CREATE TABLE organizations (id VARCHAR2(255) NOT NULL, created_by VARCHAR2(255) NOT NULL, created_on TIMESTAMP NOT NULL, description VARCHAR2(512), modified_by VARCHAR2(255) NOT NULL, modified_on TIMESTAMP NOT NULL, name VARCHAR2(255) NOT NULL);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-9::apiman (generated)
CREATE TABLE pd_templates (policydef_id VARCHAR2(255) NOT NULL, language VARCHAR2(255), template VARCHAR2(2048));

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-10::apiman (generated)
CREATE TABLE permissions (role_id VARCHAR2(255) NOT NULL, permissions INTEGER);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-11::apiman (generated)
CREATE TABLE plan_versions (id NUMBER(38, 0) NOT NULL, created_by VARCHAR2(255) NOT NULL, created_on TIMESTAMP NOT NULL, locked_on TIMESTAMP, modified_by VARCHAR2(255) NOT NULL, modified_on TIMESTAMP NOT NULL, status VARCHAR2(255) NOT NULL, version VARCHAR2(255) NOT NULL, plan_id VARCHAR2(255), plan_org_id VARCHAR2(255));

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-12::apiman (generated)
CREATE TABLE plans (id VARCHAR2(255) NOT NULL, created_by VARCHAR2(255) NOT NULL, created_on TIMESTAMP NOT NULL, description VARCHAR2(512), name VARCHAR2(255) NOT NULL, organization_id VARCHAR2(255) NOT NULL);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-13::apiman (generated)
CREATE TABLE plugins (id NUMBER(38, 0) NOT NULL, artifact_id VARCHAR2(255) NOT NULL, classifier VARCHAR2(255), created_by VARCHAR2(255) NOT NULL, created_on TIMESTAMP NOT NULL, description VARCHAR2(512), group_id VARCHAR2(255) NOT NULL, name VARCHAR2(255) NOT NULL, type VARCHAR2(255), version VARCHAR2(255) NOT NULL, deleted NUMBER(1));

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-14::apiman (generated)
CREATE TABLE policies (id NUMBER(38, 0) NOT NULL, configuration CLOB, created_by VARCHAR2(255) NOT NULL, created_on TIMESTAMP NOT NULL, entity_id VARCHAR2(255) NOT NULL, entity_version VARCHAR2(255) NOT NULL, modified_by VARCHAR2(255) NOT NULL, modified_on TIMESTAMP NOT NULL, name VARCHAR2(255) NOT NULL, order_index INTEGER NOT NULL, organization_id VARCHAR2(255) NOT NULL, type VARCHAR2(255) NOT NULL, definition_id VARCHAR2(255) NOT NULL);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-15::apiman (generated)
CREATE TABLE policydefs (id VARCHAR2(255) NOT NULL, description VARCHAR2(512) NOT NULL, form VARCHAR2(255), form_type VARCHAR2(255), icon VARCHAR2(255) NOT NULL, name VARCHAR2(255) NOT NULL, plugin_id NUMBER(38, 0), policy_impl VARCHAR2(255) NOT NULL, deleted NUMBER(1));

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-16::apiman (generated)
CREATE TABLE roles (id VARCHAR2(255) NOT NULL, auto_grant NUMBER(1), created_by VARCHAR2(255) NOT NULL, created_on TIMESTAMP NOT NULL, description VARCHAR2(512), name VARCHAR2(255));

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-17::apiman (generated)
CREATE TABLE api_defs (id NUMBER(38, 0) NOT NULL, data BLOB, api_version_id NUMBER(38, 0));

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-18::apiman (generated)
CREATE TABLE api_versions (id NUMBER(38, 0) NOT NULL, created_by VARCHAR2(255) NOT NULL, created_on TIMESTAMP NOT NULL, definition_type VARCHAR2(255), endpoint VARCHAR2(255), endpoint_type VARCHAR2(255), endpoint_ct VARCHAR2(255), modified_by VARCHAR2(255) NOT NULL, modified_on TIMESTAMP NOT NULL, public_api NUMBER(1) NOT NULL, parse_payload NUMBER(1), strip_keys NUMBER(1), published_on TIMESTAMP, retired_on TIMESTAMP, status VARCHAR2(255) NOT NULL, version VARCHAR2(255), api_id VARCHAR2(255), api_org_id VARCHAR2(255), definition_url VARCHAR2(255));

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-19::apiman (generated)
CREATE TABLE apis (id VARCHAR2(255) NOT NULL, created_by VARCHAR2(255) NOT NULL, created_on TIMESTAMP NOT NULL, description VARCHAR2(512), name VARCHAR2(255) NOT NULL, organization_id VARCHAR2(255) NOT NULL, num_published INTEGER);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-20::apiman (generated)
CREATE TABLE api_gateways (api_version_id NUMBER(38, 0) NOT NULL, gateway_id VARCHAR2(255) NOT NULL);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-21::apiman (generated)
CREATE TABLE api_plans (api_version_id NUMBER(38, 0) NOT NULL, plan_id VARCHAR2(255) NOT NULL, version VARCHAR2(255) NOT NULL);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-22::apiman (generated)
CREATE TABLE users (username VARCHAR2(255) NOT NULL, email VARCHAR2(255), full_name VARCHAR2(255), joined_on TIMESTAMP);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-23::apiman (generated)
CREATE TABLE downloads (id VARCHAR2(255) NOT NULL, type VARCHAR2(255), path VARCHAR2(255), expires TIMESTAMP);

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-24::apiman (generated)
CREATE TABLE metadata (id NUMBER(38, 0) NOT NULL, exported_on TIMESTAMP, imported_on TIMESTAMP, apiman_version VARCHAR2(255), apiman_version_at_import VARCHAR2(255), success NUMBER(1));

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-23::apiman (generated)
ALTER TABLE endpoint_properties ADD PRIMARY KEY (api_version_id, name);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-24::apiman (generated)
ALTER TABLE api_gateways ADD PRIMARY KEY (api_version_id, gateway_id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-25::apiman (generated)
ALTER TABLE api_plans ADD PRIMARY KEY (api_version_id, plan_id, version);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-26::apiman (generated)
ALTER TABLE client_versions ADD CONSTRAINT client_versionsPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-27::apiman (generated)
ALTER TABLE clients ADD CONSTRAINT clientsPK PRIMARY KEY (id, organization_id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-28::apiman (generated)
ALTER TABLE auditlog ADD CONSTRAINT auditlogPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-29::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT contractsPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-30::apiman (generated)
ALTER TABLE gateways ADD CONSTRAINT gatewaysPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-31::apiman (generated)
ALTER TABLE memberships ADD CONSTRAINT membershipsPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-32::apiman (generated)
ALTER TABLE organizations ADD CONSTRAINT organizationsPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-33::apiman (generated)
ALTER TABLE plan_versions ADD CONSTRAINT plan_versionsPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-34::apiman (generated)
ALTER TABLE plans ADD CONSTRAINT plansPK PRIMARY KEY (id, organization_id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-35::apiman (generated)
ALTER TABLE plugins ADD CONSTRAINT pluginsPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-36::apiman (generated)
ALTER TABLE policies ADD CONSTRAINT policiesPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-37::apiman (generated)
ALTER TABLE policydefs ADD CONSTRAINT policydefsPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-38::apiman (generated)
ALTER TABLE roles ADD CONSTRAINT rolesPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-39::apiman (generated)
ALTER TABLE api_defs ADD CONSTRAINT api_defsPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-40::apiman (generated)
ALTER TABLE api_versions ADD CONSTRAINT api_versionsPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-41::apiman (generated)
ALTER TABLE apis ADD CONSTRAINT apisPK PRIMARY KEY (id, organization_id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-42::apiman (generated)
ALTER TABLE users ADD CONSTRAINT usersPK PRIMARY KEY (username);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-43::apiman (generated)
ALTER TABLE apis ADD CONSTRAINT FK_31hj3xmhp1wedxjh5bklnlg15 FOREIGN KEY (organization_id) REFERENCES organizations (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-44::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT FK_6h06sgs4dudh1wehmk0us973g FOREIGN KEY (clientv_id) REFERENCES client_versions (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-45::apiman (generated)
ALTER TABLE api_defs ADD CONSTRAINT FK_81fuw1n8afmvpw4buk7l4tyxk FOREIGN KEY (api_version_id) REFERENCES api_versions (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-46::apiman (generated)
ALTER TABLE client_versions ADD CONSTRAINT FK_8epnoby31bt7xakegakigpikp FOREIGN KEY (client_id, client_org_id) REFERENCES clients (id, organization_id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-47::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT FK_8o6t1f3kg96rxy5uv51f6k9fy FOREIGN KEY (apiv_id) REFERENCES api_versions (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-48::apiman (generated)
ALTER TABLE api_versions ADD CONSTRAINT FK_92erjg9k1lni97gd87nt6tq37 FOREIGN KEY (api_id, api_org_id) REFERENCES apis (id, organization_id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-49::apiman (generated)
ALTER TABLE endpoint_properties ADD CONSTRAINT FK_gn0ydqur10sxuvpyw2jvv4xxb FOREIGN KEY (api_version_id) REFERENCES api_versions (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-50::apiman (generated)
ALTER TABLE clients ADD CONSTRAINT FK_jenpu34rtuncsgvtw0sfo8qq9 FOREIGN KEY (organization_id) REFERENCES organizations (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-51::apiman (generated)
ALTER TABLE policies ADD CONSTRAINT FK_l4q6we1bos1yl9unmogei6aja FOREIGN KEY (definition_id) REFERENCES policydefs (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-52::apiman (generated)
ALTER TABLE plans ADD CONSTRAINT FK_lwhc7xrdbsun1ak2uvfu0prj8 FOREIGN KEY (organization_id) REFERENCES organizations (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-53::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT FK_nyw8xu6m8cx4rwwbtrxbjneui FOREIGN KEY (planv_id) REFERENCES plan_versions (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-54::apiman (generated)
ALTER TABLE api_gateways ADD CONSTRAINT FK_p5dm3cngljt6yrsnvc7uc6a75 FOREIGN KEY (api_version_id) REFERENCES api_versions (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-55::apiman (generated)
ALTER TABLE pd_templates ADD CONSTRAINT FK_prbnn7j7m6m3pxt2dsn9gwlw8 FOREIGN KEY (policydef_id) REFERENCES policydefs (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-56::apiman (generated)
ALTER TABLE permissions ADD CONSTRAINT FK_sq51ihfrapwdr98uufenhcocg FOREIGN KEY (role_id) REFERENCES roles (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-57::apiman (generated)
ALTER TABLE api_plans ADD CONSTRAINT FK_t7uvfcsswopb9kh8wpa86blqr FOREIGN KEY (api_version_id) REFERENCES api_versions (id);

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-58::apiman (generated)
ALTER TABLE plan_versions ADD CONSTRAINT FK_tonylvm2ypnq3efxqr1g0m9fs FOREIGN KEY (plan_id, plan_org_id) REFERENCES plans (id, organization_id);

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-1::apiman
ALTER TABLE plugins ADD CONSTRAINT UK_plugins_1 UNIQUE (group_id, artifact_id);

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-2::apiman
ALTER TABLE memberships ADD CONSTRAINT UK_memberships_1 UNIQUE (user_id, role_id, org_id);

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-3::apiman
ALTER TABLE plan_versions ADD CONSTRAINT UK_plan_versions_1 UNIQUE (plan_id, plan_org_id, version);

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-4::apiman
ALTER TABLE client_versions ADD CONSTRAINT UK_client_versions_1 UNIQUE (client_id, client_org_id, version);

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-5::apiman
ALTER TABLE api_versions ADD CONSTRAINT UK_api_versions_1 UNIQUE (api_id, api_org_id, version);

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-6::apiman
ALTER TABLE api_defs ADD CONSTRAINT UK_api_defs_1 UNIQUE (api_version_id);

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-7::apiman
ALTER TABLE contracts ADD CONSTRAINT UK_contracts_1 UNIQUE (clientv_id, apiv_id);

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-8::apiman
ALTER TABLE client_versions ADD CONSTRAINT UK_client_versions_2 UNIQUE (apikey);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-1::apiman
CREATE INDEX IDX_auditlog_1 ON auditlog(who);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-2::apiman
CREATE INDEX IDX_auditlog_2 ON auditlog(organization_id, entity_id, entity_version, entity_type);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-3::apiman
CREATE INDEX IDX_FK_pd_templates_1 ON pd_templates(policydef_id);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-4::apiman
CREATE INDEX IDX_users_1 ON users(username);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-5::apiman
CREATE INDEX IDX_users_2 ON users(full_name);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-6::apiman
CREATE INDEX IDX_FK_permissions_1 ON permissions(role_id);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-7::apiman
CREATE INDEX IDX_memberships_1 ON memberships(user_id);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-8::apiman
CREATE INDEX IDX_organizations_1 ON organizations(name);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-9::apiman
CREATE INDEX IDX_FK_plans_1 ON plans(organization_id);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-10::apiman
CREATE INDEX IDX_FK_clients_1 ON clients(organization_id);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-11::apiman
CREATE INDEX IDX_apis_1 ON apis(name);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-12::apiman
CREATE INDEX IDX_FK_apis_1 ON apis(organization_id);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-13::apiman
CREATE INDEX IDX_policies_1 ON policies(organization_id, entity_id, entity_version);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-14::apiman
CREATE INDEX IDX_policies_2 ON policies(order_index);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-15::apiman
CREATE INDEX IDX_FK_policies_1 ON policies(definition_id);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-16::apiman
CREATE INDEX IDX_FK_contracts_p ON contracts(planv_id);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-17::apiman
CREATE INDEX IDX_FK_contracts_s ON contracts(apiv_id);

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-18::apiman
CREATE INDEX IDX_FK_contracts_a ON contracts(clientv_id);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-6::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE developer_mappings (developer_id VARCHAR2(255) NOT NULL, client_id VARCHAR2(255) NOT NULL, organization_id VARCHAR2(255) NOT NULL, CONSTRAINT PK_DEVELOPER_MAPPINGS PRIMARY KEY (developer_id, client_id, organization_id));

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-7::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE developers (id VARCHAR2(255) NOT NULL, CONSTRAINT developersPK PRIMARY KEY (id));

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-8::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE notification_category_preferences (NotificationPreferenceEntity_id NUMBER(38, 0) NOT NULL, category VARCHAR2(255));

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-9::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE notification_preferences (id NUMBER(38, 0) GENERATED BY DEFAULT AS IDENTITY NOT NULL, type VARCHAR2(255) NOT NULL, userId VARCHAR2(255) NOT NULL, CONSTRAINT notification_preferencesPK PRIMARY KEY (id));

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-10::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE notification_types (type VARCHAR2(255) NOT NULL, description VARCHAR2(255) NOT NULL, CONSTRAINT notification_typesPK PRIMARY KEY (type));

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-11::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE notifications (id NUMBER(38, 0) GENERATED BY DEFAULT AS IDENTITY NOT NULL, category VARCHAR2(255) NOT NULL, created_on TIMESTAMP, modified_on TIMESTAMP, payload JSON NOT NULL, reason VARCHAR2(255) NOT NULL, reason_message VARCHAR2(255) NOT NULL, recipient VARCHAR2(255) NOT NULL, source VARCHAR2(255) NOT NULL, status VARCHAR2(255) NOT NULL, CONSTRAINT notificationsPK PRIMARY KEY (id));

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-12::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE api_plans ADD expose_in_portal NUMBER(1) NOT NULL;

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-13::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE api_versions ADD expose_in_portal NUMBER(1) NOT NULL;

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-14::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE api_versions ADD extended_description NCLOB;

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-15::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE apis ADD image_file_ref VARCHAR2(255);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-16::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE clients ADD image_file_ref VARCHAR2(255);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-17::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE api_plans ADD requires_approval NUMBER(1) NOT NULL;

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-18::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE contracts ADD status INTEGER NOT NULL;

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-19::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE downloads ADD CONSTRAINT downloadsPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-20::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE metadata ADD CONSTRAINT metadataPK PRIMARY KEY (id);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-21::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE plan_versions ADD CONSTRAINT UK53ss55b2hiye2e9v6lftdo5jg UNIQUE (plan_id, plan_org_id, version);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-22::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE memberships ADD CONSTRAINT UKfwy6c61bcnvshcsxubanjvi08 UNIQUE (user_id, role_id, org_id);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-23::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE plugins ADD CONSTRAINT UKofbok9ushig9vviq01dnu11x UNIQUE (group_id, artifact_id);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-24::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE notification_preferences ADD CONSTRAINT UserAllowedOnlyOneOfEachNotificationType UNIQUE (userId, type);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-25::msavy marc@blackparrotlabs.io (generated)
CREATE UNIQUE INDEX IX_null ON api_plans(api_version_id, expose_in_portal, plan_id, requires_approval, version);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-26::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE notification_category_preferences ADD CONSTRAINT FKaq4x0n83d83xevui0ctqwdgbi FOREIGN KEY (NotificationPreferenceEntity_id) REFERENCES notification_preferences (id);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-27::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE developer_mappings ADD CONSTRAINT FKhl2dwc4m0kvisedxfb9crceqd FOREIGN KEY (developer_id) REFERENCES developers (id);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633441143380-4::msavy (generated)
CREATE TABLE KeyValueTag (id NUMBER(38, 0) GENERATED BY DEFAULT AS IDENTITY NOT NULL, key VARCHAR2(255) NOT NULL, value VARCHAR2(255), CONSTRAINT KeyValueTagPK PRIMARY KEY (id));

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633441143380-5::msavy (generated)
CREATE TABLE api_tag (api_id VARCHAR2(255) NOT NULL, org_id VARCHAR2(255) NOT NULL, tag_id NUMBER(38, 0) NOT NULL, CONSTRAINT PK_API_TAG PRIMARY KEY (api_id, org_id, tag_id));

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-6::msavy (generated)
CREATE TABLE blob_store (id VARCHAR2(255) NOT NULL, mrblobby BLOB NOT NULL, created_on TIMESTAMP NOT NULL, hash NUMBER(38, 0) NOT NULL, mime_type VARCHAR2(255) NOT NULL, modified_on TIMESTAMP NOT NULL, name VARCHAR2(255) NOT NULL, references INTEGER NOT NULL, CONSTRAINT blob_storePK PRIMARY KEY (id));

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-7::msavy (generated)
CREATE TABLE outbox (id NUMBER(38, 0) GENERATED BY DEFAULT AS IDENTITY NOT NULL, event_version NUMBER(38, 0) NOT NULL, payload RAW NOT NULL, source VARCHAR2(255) NOT NULL, subject VARCHAR2(255) NOT NULL, time TIMESTAMP NOT NULL, type VARCHAR2(255) NOT NULL, CONSTRAINT outboxPK PRIMARY KEY (id));

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-8::msavy (generated)
ALTER TABLE blob_store ADD CONSTRAINT UC_BLOB_STOREID_COL UNIQUE (id);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-10::msavy (generated)
ALTER TABLE blob_store ADD CONSTRAINT UK_4jee67ekw7s4y8spoc58i4dsf UNIQUE (hash, mime_type, name);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-15::msavy (generated)
ALTER TABLE api_tag ADD CONSTRAINT FK2h64maqscweorti1hta9josl2 FOREIGN KEY (tag_id) REFERENCES KeyValueTag (id);

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-16::msavy (generated)
ALTER TABLE api_tag ADD CONSTRAINT FKlpr8yu65omneju5297uqthb6k FOREIGN KEY (api_id, org_id) REFERENCES apis (id, organization_id);

