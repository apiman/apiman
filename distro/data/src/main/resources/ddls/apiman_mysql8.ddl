--  *********************************************************************
--  Update Database Script
--  *********************************************************************
--  Change Log: /Users/msavy/oss/apiman/apiman/distro/ddl/src/main/liquibase/master.xml
--  Ran at: 29/04/2022, 13:18
--  Against: apiman@offline:mysql?version=8&caseSensitive=true&catalog=apiman&changeLogFile=/Users/msavy/oss/apiman/apiman/distro/ddl/target/changelog/mysql/databasechangelog.csv
--  Liquibase version: 4.6.2
--  *********************************************************************

--  Changeset src/main/liquibase/current/000-apiman-manager-api.db.sequences.changelog.xml::1434723514712-1::apiman
CREATE TABLE hibernate_sequence (next_val bigint(20) DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO hibernate_sequence VALUES (999);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-1::apiman (generated)
CREATE TABLE client_versions (id BIGINT NOT NULL, created_by VARCHAR(255) NOT NULL, created_on timestamp NOT NULL, modified_by VARCHAR(255) NOT NULL, modified_on timestamp NOT NULL, published_on timestamp NULL, retired_on timestamp NULL, status VARCHAR(255) NOT NULL, version VARCHAR(255) NOT NULL, client_id VARCHAR(255) NULL, client_org_id VARCHAR(255) NULL, apikey VARCHAR(255) NOT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-2::apiman (generated)
CREATE TABLE clients (id VARCHAR(255) NOT NULL, created_by VARCHAR(255) NOT NULL, created_on timestamp NOT NULL, `description` VARCHAR(512) NULL, name VARCHAR(255) NOT NULL, organization_id VARCHAR(255) NOT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-3::apiman (generated)
CREATE TABLE auditlog (id BIGINT NOT NULL, created_on timestamp NOT NULL, data LONGTEXT NULL, entity_id VARCHAR(255) NULL, entity_type VARCHAR(255) NOT NULL, entity_version VARCHAR(255) NULL, organization_id VARCHAR(255) NOT NULL, what VARCHAR(255) NOT NULL, who VARCHAR(255) NOT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-4::apiman (generated)
CREATE TABLE contracts (id BIGINT NOT NULL, created_by VARCHAR(255) NOT NULL, created_on timestamp NOT NULL, clientv_id BIGINT NULL, planv_id BIGINT NULL, apiv_id BIGINT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-5::apiman (generated)
CREATE TABLE endpoint_properties (api_version_id BIGINT NOT NULL, value VARCHAR(255) NULL, name VARCHAR(255) NOT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-6::apiman (generated)
CREATE TABLE gateways (id VARCHAR(255) NOT NULL, configuration LONGTEXT NOT NULL, created_by VARCHAR(255) NOT NULL, created_on timestamp NOT NULL, `description` VARCHAR(512) NULL, modified_by VARCHAR(255) NOT NULL, modified_on timestamp NOT NULL, name VARCHAR(255) NOT NULL, type VARCHAR(255) NOT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-7::apiman (generated)
CREATE TABLE memberships (id BIGINT NOT NULL, created_on timestamp NULL, org_id VARCHAR(255) NULL, role_id VARCHAR(255) NULL, user_id VARCHAR(255) NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-8::apiman (generated)
CREATE TABLE organizations (id VARCHAR(255) NOT NULL, created_by VARCHAR(255) NOT NULL, created_on timestamp NOT NULL, `description` VARCHAR(512) NULL, modified_by VARCHAR(255) NOT NULL, modified_on timestamp NOT NULL, name VARCHAR(255) NOT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-9::apiman (generated)
CREATE TABLE pd_templates (policydef_id VARCHAR(255) NOT NULL, language VARCHAR(255) NULL, template VARCHAR(2048) NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-10::apiman (generated)
CREATE TABLE permissions (role_id VARCHAR(255) NOT NULL, permissions INT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-11::apiman (generated)
CREATE TABLE plan_versions (id BIGINT NOT NULL, created_by VARCHAR(255) NOT NULL, created_on timestamp NOT NULL, locked_on timestamp NULL, modified_by VARCHAR(255) NOT NULL, modified_on timestamp NOT NULL, status VARCHAR(255) NOT NULL, version VARCHAR(255) NOT NULL, plan_id VARCHAR(255) NULL, plan_org_id VARCHAR(255) NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-12::apiman (generated)
CREATE TABLE plans (id VARCHAR(255) NOT NULL, created_by VARCHAR(255) NOT NULL, created_on timestamp NOT NULL, `description` VARCHAR(512) NULL, name VARCHAR(255) NOT NULL, organization_id VARCHAR(255) NOT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-13::apiman (generated)
CREATE TABLE plugins (id BIGINT NOT NULL, artifact_id VARCHAR(255) NOT NULL, classifier VARCHAR(255) NULL, created_by VARCHAR(255) NOT NULL, created_on timestamp NOT NULL, `description` VARCHAR(512) NULL, group_id VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL, type VARCHAR(255) NULL, version VARCHAR(255) NOT NULL, deleted BIT(1) NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-14::apiman (generated)
CREATE TABLE policies (id BIGINT NOT NULL, configuration LONGTEXT NULL, created_by VARCHAR(255) NOT NULL, created_on timestamp NOT NULL, entity_id VARCHAR(255) NOT NULL, entity_version VARCHAR(255) NOT NULL, modified_by VARCHAR(255) NOT NULL, modified_on timestamp NOT NULL, name VARCHAR(255) NOT NULL, order_index INT NOT NULL, organization_id VARCHAR(255) NOT NULL, type VARCHAR(255) NOT NULL, definition_id VARCHAR(255) NOT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-15::apiman (generated)
CREATE TABLE policydefs (id VARCHAR(255) NOT NULL, `description` VARCHAR(512) NOT NULL, form VARCHAR(255) NULL, form_type VARCHAR(255) NULL, icon VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL, plugin_id BIGINT NULL, policy_impl VARCHAR(255) NOT NULL, deleted BIT(1) NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-16::apiman (generated)
CREATE TABLE roles (id VARCHAR(255) NOT NULL, auto_grant BIT(1) NULL, created_by VARCHAR(255) NOT NULL, created_on timestamp NOT NULL, `description` VARCHAR(512) NULL, name VARCHAR(255) NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-17::apiman (generated)
CREATE TABLE api_defs (id BIGINT NOT NULL, data LONGBLOB NULL, api_version_id BIGINT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-18::apiman (generated)
CREATE TABLE api_versions (id BIGINT NOT NULL, created_by VARCHAR(255) NOT NULL, created_on timestamp NOT NULL, definition_type VARCHAR(255) NULL, endpoint VARCHAR(255) NULL, endpoint_type VARCHAR(255) NULL, endpoint_ct VARCHAR(255) NULL, modified_by VARCHAR(255) NOT NULL, modified_on timestamp NOT NULL, public_api BIT(1) NOT NULL, parse_payload BIT(1) NULL, strip_keys BIT(1) NULL, published_on timestamp NULL, retired_on timestamp NULL, status VARCHAR(255) NOT NULL, version VARCHAR(255) NULL, api_id VARCHAR(255) NULL, api_org_id VARCHAR(255) NULL, definition_url VARCHAR(255) NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-19::apiman (generated)
CREATE TABLE apis (id VARCHAR(255) NOT NULL, created_by VARCHAR(255) NOT NULL, created_on timestamp NOT NULL, `description` VARCHAR(512) NULL, name VARCHAR(255) NOT NULL, organization_id VARCHAR(255) NOT NULL, num_published INT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-20::apiman (generated)
CREATE TABLE api_gateways (api_version_id BIGINT NOT NULL, gateway_id VARCHAR(255) NOT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-21::apiman (generated)
CREATE TABLE api_plans (api_version_id BIGINT NOT NULL, plan_id VARCHAR(255) NOT NULL, version VARCHAR(255) NOT NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-22::apiman (generated)
CREATE TABLE users (username VARCHAR(255) NOT NULL, email VARCHAR(255) NULL, full_name VARCHAR(255) NULL, joined_on timestamp NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-23::apiman (generated)
CREATE TABLE downloads (id VARCHAR(255) NOT NULL, type VARCHAR(255) NULL, `path` VARCHAR(255) NULL, expires timestamp NULL);

--  Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-24::apiman (generated)
CREATE TABLE metadata (id BIGINT NOT NULL, exported_on timestamp NULL, imported_on timestamp NULL, apiman_version VARCHAR(255) NULL, apiman_version_at_import VARCHAR(255) NULL, success BIT(1) NULL);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-23::apiman (generated)
ALTER TABLE endpoint_properties ADD PRIMARY KEY (api_version_id, name);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-24::apiman (generated)
ALTER TABLE api_gateways ADD PRIMARY KEY (api_version_id, gateway_id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-25::apiman (generated)
ALTER TABLE api_plans ADD PRIMARY KEY (api_version_id, plan_id, version);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-26::apiman (generated)
ALTER TABLE client_versions ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-27::apiman (generated)
ALTER TABLE clients ADD PRIMARY KEY (id, organization_id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-28::apiman (generated)
ALTER TABLE auditlog ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-29::apiman (generated)
ALTER TABLE contracts ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-30::apiman (generated)
ALTER TABLE gateways ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-31::apiman (generated)
ALTER TABLE memberships ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-32::apiman (generated)
ALTER TABLE organizations ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-33::apiman (generated)
ALTER TABLE plan_versions ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-34::apiman (generated)
ALTER TABLE plans ADD PRIMARY KEY (id, organization_id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-35::apiman (generated)
ALTER TABLE plugins ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-36::apiman (generated)
ALTER TABLE policies ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-37::apiman (generated)
ALTER TABLE policydefs ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-38::apiman (generated)
ALTER TABLE roles ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-39::apiman (generated)
ALTER TABLE api_defs ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-40::apiman (generated)
ALTER TABLE api_versions ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-41::apiman (generated)
ALTER TABLE apis ADD PRIMARY KEY (id, organization_id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-42::apiman (generated)
ALTER TABLE users ADD PRIMARY KEY (username);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-43::apiman (generated)
ALTER TABLE apis ADD CONSTRAINT FK_31hj3xmhp1wedxjh5bklnlg15 FOREIGN KEY (organization_id) REFERENCES organizations (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-44::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT FK_6h06sgs4dudh1wehmk0us973g FOREIGN KEY (clientv_id) REFERENCES client_versions (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-45::apiman (generated)
ALTER TABLE api_defs ADD CONSTRAINT FK_81fuw1n8afmvpw4buk7l4tyxk FOREIGN KEY (api_version_id) REFERENCES api_versions (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-46::apiman (generated)
ALTER TABLE client_versions ADD CONSTRAINT FK_8epnoby31bt7xakegakigpikp FOREIGN KEY (client_id, client_org_id) REFERENCES clients (id, organization_id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-47::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT FK_8o6t1f3kg96rxy5uv51f6k9fy FOREIGN KEY (apiv_id) REFERENCES api_versions (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-48::apiman (generated)
ALTER TABLE api_versions ADD CONSTRAINT FK_92erjg9k1lni97gd87nt6tq37 FOREIGN KEY (api_id, api_org_id) REFERENCES apis (id, organization_id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-49::apiman (generated)
ALTER TABLE endpoint_properties ADD CONSTRAINT FK_gn0ydqur10sxuvpyw2jvv4xxb FOREIGN KEY (api_version_id) REFERENCES api_versions (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-50::apiman (generated)
ALTER TABLE clients ADD CONSTRAINT FK_jenpu34rtuncsgvtw0sfo8qq9 FOREIGN KEY (organization_id) REFERENCES organizations (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-51::apiman (generated)
ALTER TABLE policies ADD CONSTRAINT FK_l4q6we1bos1yl9unmogei6aja FOREIGN KEY (definition_id) REFERENCES policydefs (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-52::apiman (generated)
ALTER TABLE plans ADD CONSTRAINT FK_lwhc7xrdbsun1ak2uvfu0prj8 FOREIGN KEY (organization_id) REFERENCES organizations (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-53::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT FK_nyw8xu6m8cx4rwwbtrxbjneui FOREIGN KEY (planv_id) REFERENCES plan_versions (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-54::apiman (generated)
ALTER TABLE api_gateways ADD CONSTRAINT FK_p5dm3cngljt6yrsnvc7uc6a75 FOREIGN KEY (api_version_id) REFERENCES api_versions (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-55::apiman (generated)
ALTER TABLE pd_templates ADD CONSTRAINT FK_prbnn7j7m6m3pxt2dsn9gwlw8 FOREIGN KEY (policydef_id) REFERENCES policydefs (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-56::apiman (generated)
ALTER TABLE permissions ADD CONSTRAINT FK_sq51ihfrapwdr98uufenhcocg FOREIGN KEY (role_id) REFERENCES roles (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-57::apiman (generated)
ALTER TABLE api_plans ADD CONSTRAINT FK_t7uvfcsswopb9kh8wpa86blqr FOREIGN KEY (api_version_id) REFERENCES api_versions (id);

--  Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-58::apiman (generated)
ALTER TABLE plan_versions ADD CONSTRAINT FK_tonylvm2ypnq3efxqr1g0m9fs FOREIGN KEY (plan_id, plan_org_id) REFERENCES plans (id, organization_id);

--  Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-1::apiman
ALTER TABLE plugins ADD CONSTRAINT UK_plugins_1 UNIQUE (group_id, artifact_id);

--  Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-2::apiman
ALTER TABLE memberships ADD CONSTRAINT UK_memberships_1 UNIQUE (user_id, role_id, org_id);

--  Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-3::apiman
ALTER TABLE plan_versions ADD CONSTRAINT UK_plan_versions_1 UNIQUE (plan_id, plan_org_id, version);

--  Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-4::apiman
ALTER TABLE client_versions ADD CONSTRAINT UK_client_versions_1 UNIQUE (client_id, client_org_id, version);

--  Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-5::apiman
ALTER TABLE api_versions ADD CONSTRAINT UK_api_versions_1 UNIQUE (api_id, api_org_id, version);

--  Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-6::apiman
ALTER TABLE api_defs ADD CONSTRAINT UK_api_defs_1 UNIQUE (api_version_id);

--  Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-7::apiman
ALTER TABLE contracts ADD CONSTRAINT UK_contracts_1 UNIQUE (clientv_id, apiv_id);

--  Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-8::apiman
ALTER TABLE client_versions ADD CONSTRAINT UK_client_versions_2 UNIQUE (apikey);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-1::apiman
CREATE INDEX IDX_auditlog_1 ON auditlog(who);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-2::apiman
CREATE INDEX IDX_auditlog_2 ON auditlog(organization_id, entity_id, entity_version, entity_type);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-3::apiman
CREATE INDEX IDX_FK_pd_templates_1 ON pd_templates(policydef_id);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-4::apiman
CREATE INDEX IDX_users_1 ON users(username);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-5::apiman
CREATE INDEX IDX_users_2 ON users(full_name);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-6::apiman
CREATE INDEX IDX_FK_permissions_1 ON permissions(role_id);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-7::apiman
CREATE INDEX IDX_memberships_1 ON memberships(user_id);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-8::apiman
CREATE INDEX IDX_organizations_1 ON organizations(name);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-9::apiman
CREATE INDEX IDX_FK_plans_1 ON plans(organization_id);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-10::apiman
CREATE INDEX IDX_FK_clients_1 ON clients(organization_id);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-11::apiman
CREATE INDEX IDX_apis_1 ON apis(name);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-12::apiman
CREATE INDEX IDX_FK_apis_1 ON apis(organization_id);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-13::apiman
CREATE INDEX IDX_policies_1 ON policies(organization_id, entity_id, entity_version);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-14::apiman
CREATE INDEX IDX_policies_2 ON policies(order_index);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-15::apiman
CREATE INDEX IDX_FK_policies_1 ON policies(definition_id);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-16::apiman
CREATE INDEX IDX_FK_contracts_p ON contracts(planv_id);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-17::apiman
CREATE INDEX IDX_FK_contracts_s ON contracts(apiv_id);

--  Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-18::apiman
CREATE INDEX IDX_FK_contracts_a ON contracts(clientv_id);

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-6::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE developer_mappings (developer_id VARCHAR(255) NOT NULL, client_id VARCHAR(255) NOT NULL, organization_id VARCHAR(255) NOT NULL, CONSTRAINT PK_DEVELOPER_MAPPINGS PRIMARY KEY (developer_id, client_id, organization_id));

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-7::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE developers (id VARCHAR(255) NOT NULL, CONSTRAINT developersPK PRIMARY KEY (id));

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-8::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE notification_category_preferences (NotificationPreferenceEntity_id BIGINT NOT NULL, category VARCHAR(255) NULL);

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-9::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE notification_preferences (id BIGINT AUTO_INCREMENT NOT NULL, type VARCHAR(255) NOT NULL, user_id VARCHAR(255) NOT NULL, CONSTRAINT notification_preferencesPK PRIMARY KEY (id));

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-10::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE notification_types (type VARCHAR(255) NOT NULL, `description` VARCHAR(255) NOT NULL, CONSTRAINT notification_typesPK PRIMARY KEY (type));

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-11::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE notifications (id BIGINT AUTO_INCREMENT NOT NULL, category VARCHAR(255) NOT NULL, created_on timestamp NULL, modified_on timestamp NULL, payload JSON NOT NULL, reason VARCHAR(255) NOT NULL, reason_message VARCHAR(255) NOT NULL, recipient VARCHAR(255) NOT NULL, source VARCHAR(255) NOT NULL, status VARCHAR(255) NOT NULL, CONSTRAINT notificationsPK PRIMARY KEY (id));

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-14::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE api_versions ADD extended_description LONGTEXT CHARACTER SET utf8 NULL;

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-15::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE apis ADD image_file_ref VARCHAR(255) NULL;

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-16::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE clients ADD image_file_ref VARCHAR(255) NULL;

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-17::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE api_plans ADD requires_approval BIT(1) NOT NULL;

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-18::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE contracts ADD status INT NOT NULL;

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-19::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE downloads ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-20::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE metadata ADD PRIMARY KEY (id);

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-21::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE plan_versions ADD CONSTRAINT UK53ss55b2hiye2e9v6lftdo5jg UNIQUE (plan_id, plan_org_id, version);

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-22::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE memberships ADD CONSTRAINT UKfwy6c61bcnvshcsxubanjvi08 UNIQUE (user_id, role_id, org_id);

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-23::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE plugins ADD CONSTRAINT UKofbok9ushig9vviq01dnu11x UNIQUE (group_id, artifact_id);

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-24::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE notification_preferences ADD CONSTRAINT UserAllowedOnlyOneOfEachNotificationType UNIQUE (user_id, type);

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-26::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE notification_category_preferences ADD CONSTRAINT FKaq4x0n83d83xevui0ctqwdgbi FOREIGN KEY (NotificationPreferenceEntity_id) REFERENCES notification_preferences (id);

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-27::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE developer_mappings ADD CONSTRAINT FKhl2dwc4m0kvisedxfb9crceqd FOREIGN KEY (developer_id) REFERENCES developers (id);

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633441143380-4::msavy (generated)
CREATE TABLE kv_tags (id BIGINT AUTO_INCREMENT NOT NULL, `key` VARCHAR(255) NOT NULL, value VARCHAR(255) NULL, CONSTRAINT kv_tagsPK PRIMARY KEY (id));

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633441143380-5::msavy (generated)
CREATE TABLE api_tag (api_id VARCHAR(255) NOT NULL, org_id VARCHAR(255) NOT NULL, tag_id BIGINT NOT NULL, CONSTRAINT PK_API_TAG PRIMARY KEY (api_id, org_id, tag_id));

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-6::msavy (generated)
CREATE TABLE blob_store (id VARCHAR(255) NOT NULL, mrblobby BLOB NOT NULL, created_on timestamp NOT NULL, hash BIGINT NOT NULL, mime_type VARCHAR(255) NOT NULL, modified_on timestamp NOT NULL, name VARCHAR(255) NOT NULL, ref_count INT NOT NULL, CONSTRAINT blob_storePK PRIMARY KEY (id));

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-7::msavy (generated)
CREATE TABLE outbox (id BIGINT AUTO_INCREMENT NOT NULL, event_version BIGINT NOT NULL, payload JSON NOT NULL, source VARCHAR(255) NOT NULL, subject VARCHAR(255) NOT NULL, time timestamp NOT NULL, type VARCHAR(255) NOT NULL, CONSTRAINT outboxPK PRIMARY KEY (id));

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-8::msavy (generated)
ALTER TABLE blob_store ADD CONSTRAINT UC_BLOB_STOREID_COL UNIQUE (id);

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-10::msavy (generated)
ALTER TABLE blob_store ADD CONSTRAINT UK_4jee67ekw7s4y8spoc58i4dsf UNIQUE (hash, mime_type, name);

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-15::msavy (generated)
ALTER TABLE api_tag ADD CONSTRAINT FK2h64maqscweorti1hta9josl2 FOREIGN KEY (tag_id) REFERENCES kv_tags (id);

--  Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-16::msavy (generated)
ALTER TABLE api_tag ADD CONSTRAINT FKlpr8yu65omneju5297uqthb6k FOREIGN KEY (api_id, org_id) REFERENCES apis (id, organization_id);

--  Changeset src/main/liquibase/current/20211206-add-locale-to-user-profile.xml::add-locale-to-use-profile::msavy marc@blackparrotlabs.io (manual changeset)
ALTER TABLE users ADD locale VARCHAR(255) NULL;

--  Changeset src/main/liquibase/current/20220228-rework-notification-filtering.xml::1646057700977-6::msavy (generated)
CREATE TABLE notification_rules (NotificationPreferenceEntity_id BIGINT NOT NULL, enabled BIT(1) NULL, expression VARCHAR(255) NULL, message VARCHAR(255) NULL, source VARCHAR(255) NULL);

--  Changeset src/main/liquibase/current/20220228-rework-notification-filtering.xml::1646057700977-7::msavy (generated)
ALTER TABLE notification_rules ADD CONSTRAINT FKbxdud6qk8e28eq1mjihqauybo FOREIGN KEY (NotificationPreferenceEntity_id) REFERENCES notification_preferences (id);

--  Changeset src/main/liquibase/current/20220228-rework-notification-filtering.xml::1646057700977-8::msavy (generated)
ALTER TABLE notification_category_preferences DROP FOREIGN KEY FKaq4x0n83d83xevui0ctqwdgbi;

--  Changeset src/main/liquibase/current/20220228-rework-notification-filtering.xml::1646057700977-10::msavy (generated)
DROP TABLE notification_category_preferences;

--  Changeset src/main/liquibase/current/20220228-rework-notification-filtering.xml::1646057700977-11::msavy (generated)
DROP TABLE notification_types;

--  Changeset src/main/liquibase/current/20220228-rework-notification-filtering.xml::1646232783603-7::msavy (generated)
ALTER TABLE notification_preferences ADD CONSTRAINT FKt9qjvmcl36i14utm5uptyqg84 FOREIGN KEY (user_id) REFERENCES users (username);

--  Changeset src/main/liquibase/current/20220330-discoverability.xml::1647015740776-8::msavy (generated)
ALTER TABLE api_plans ADD discoverability VARCHAR(255) DEFAULT 'ORG_MEMBERS' NULL;

ALTER TABLE api_versions ADD discoverability VARCHAR(255) DEFAULT 'ORG_MEMBERS' NULL;

--  Changeset src/main/liquibase/current/20220330-discoverability.xml::1646489262610-4::msavy (generated)
CREATE TABLE discoverability (id VARCHAR(255) NOT NULL, org_id VARCHAR(255) NULL, api_id VARCHAR(255) NULL, api_version VARCHAR(255) NULL, plan_id VARCHAR(255) NULL, plan_version VARCHAR(255) NULL, discoverability VARCHAR(255) NULL, CONSTRAINT discoverabilityPK PRIMARY KEY (id));

CREATE INDEX api_plan_discoverability_index ON discoverability(org_id, api_id, api_version, plan_id, plan_version);

CREATE INDEX api_version_discoverability_index ON discoverability(org_id, api_id, api_version);

--  Changeset src/main/liquibase/current/20220330-discoverability.xml::discoverability-view-trigger::msavy
--  A hand-rolled materialized view that synchronises changes to 'discoverability' on `api_plans` and `api_versions` to `discoverability`
--              This enables very efficient search without performing multiple joins, plus avoids significantly complicating queries by having to
--              reference all different locations that `discoverability` can be set.
--  
--              Plausibly we may need to add additional location(s) in future such as `organization`, which should be mostly copy-and-paste.
--  
--              A nice alternative to this would be CDC with something like Debezium, but that requires the DB to have been set up properly (sometimes
--              including special plugins), which makes the deployment more complicated and difficult.
--  
--              Materialized views were considered, but these have extremely variable functionality on different DBs. For example, on Postgres all
--              materialized views must be manually updated using a special SQL command. There is no baked-in commit or time-based refresh.
-- API Plans
CREATE TRIGGER insert_apiplan_into_discoverability AFTER INSERT ON api_plans
FOR EACH ROW BEGIN
    INSERT INTO discoverability(id, org_id, api_id, api_version, plan_id, plan_version, discoverability)
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS
    (
        SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
        FROM api_versions av
        WHERE av.id = NEW.api_version_id
    )
    SELECT
        CONCAT_WS(':',
            Api_Version_CTE.api_org_id,
            Api_Version_CTE.api_id,
            Api_Version_CTE.api_version,
            NEW.plan_id,
            NEW.version
        ),
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        NEW.plan_id,
        NEW.version,
        NEW.discoverability
    FROM Api_Version_CTE;

END;

CREATE TRIGGER update_apiplan_into_discoverability AFTER UPDATE ON api_plans
FOR EACH ROW BEGIN
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS (
         SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
         FROM api_versions av
         WHERE av.id = NEW.api_version_id
    )
    UPDATE discoverability
    SET org_id = Api_Version_CTE.api_org_id,
        api_id = Api_Version_CTE.api_id,
        api_version = Api_Version_CTE.api_version,
        plan_id = NEW.plan_id,
        plan_version = NEW.version,
        discoverability = NEW.discoverability
    WHERE id = CONCAT_WS(':',
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        NEW.plan_id,
        NEW.version
    );

END;

CREATE TRIGGER api_plan_discoverability_trigger_delete AFTER DELETE ON api_plans
FOR EACH ROW
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS
    (
        SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
        FROM api_versions av
        WHERE av.id = OLD.api_version_id
    )
    DELETE FROM discoverability
    USING Api_Version_CTE, discoverability
    WHERE discoverability.id = CONCAT_WS(':',
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        OLD.plan_id,
        OLD.version
    );

END;

-- API Versions
CREATE TRIGGER insert_apiversion_into_discoverability AFTER INSERT ON api_versions
FOR EACH ROW BEGIN
    INSERT INTO discoverability(id, org_id, api_id, api_version, plan_id, plan_version, discoverability)
    VALUES (
        CONCAT_WS(':', NEW.api_org_id, NEW.api_id, NEW.version),
        NEW.api_org_id,
        NEW.api_id,
        NEW.version,
        NULL,
        NULL,
        NEW.discoverability
    );

END;

CREATE TRIGGER update_apiversion_into_discoverability AFTER UPDATE ON api_versions
FOR EACH ROW BEGIN
    UPDATE discoverability
    SET org_id = NEW.api_org_id,
        api_id = NEW.api_id,
        api_version = NEW.version,
        plan_id = NULL,
        plan_version = NULL,
        discoverability = NEW.discoverability
    WHERE id = CONCAT_WS(':', NEW.api_org_id, NEW.api_id, NEW.version);

END;

CREATE TRIGGER delete_apiversion_from_discoverability AFTER DELETE ON api_versions
FOR EACH ROW BEGIN
    DELETE FROM discoverability d WHERE d.id = CONCAT_WS(':', OLD.api_org_id, OLD.api_id, OLD.version);

END;

