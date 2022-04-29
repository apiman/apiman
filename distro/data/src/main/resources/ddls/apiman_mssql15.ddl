-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: /Users/msavy/oss/apiman/apiman/distro/ddl/src/main/liquibase/master.xml
-- Ran at: 29/04/2022, 13:23
-- Against: apiman@offline:mssql?version=15&caseSensitive=true&catalog=apiman&changeLogFile=/Users/msavy/oss/apiman/apiman/distro/ddl/target/changelog/mssql/databasechangelog.csv
-- Liquibase version: 4.6.2
-- *********************************************************************

USE apiman;
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-1::apiman (generated)
CREATE TABLE client_versions (id bigint NOT NULL, created_by varchar(255) NOT NULL, created_on datetime2 NOT NULL, modified_by varchar(255) NOT NULL, modified_on datetime2 NOT NULL, published_on datetime2, retired_on datetime2, status varchar(255) NOT NULL, version varchar(255) NOT NULL, client_id varchar(255), client_org_id varchar(255), apikey varchar(255) NOT NULL)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-2::apiman (generated)
CREATE TABLE clients (id varchar(255) NOT NULL, created_by varchar(255) NOT NULL, created_on datetime2 NOT NULL, description varchar(512), name varchar(255) NOT NULL, organization_id varchar(255) NOT NULL)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-3::apiman (generated)
CREATE TABLE auditlog (id bigint NOT NULL, created_on datetime2 NOT NULL, data varchar(MAX), entity_id varchar(255), entity_type varchar(255) NOT NULL, entity_version varchar(255), organization_id varchar(255) NOT NULL, what varchar(255) NOT NULL, who varchar(255) NOT NULL)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-4::apiman (generated)
CREATE TABLE contracts (id bigint NOT NULL, created_by varchar(255) NOT NULL, created_on datetime2 NOT NULL, clientv_id bigint, planv_id bigint, apiv_id bigint)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-5::apiman (generated)
CREATE TABLE endpoint_properties (api_version_id bigint NOT NULL, value varchar(255), name varchar(255) NOT NULL)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-6::apiman (generated)
CREATE TABLE gateways (id varchar(255) NOT NULL, configuration varchar(MAX) NOT NULL, created_by varchar(255) NOT NULL, created_on datetime2 NOT NULL, description varchar(512), modified_by varchar(255) NOT NULL, modified_on datetime2 NOT NULL, name varchar(255) NOT NULL, type varchar(255) NOT NULL)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-7::apiman (generated)
CREATE TABLE memberships (id bigint NOT NULL, created_on datetime2, org_id varchar(255), role_id varchar(255), user_id varchar(255))
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-8::apiman (generated)
CREATE TABLE organizations (id varchar(255) NOT NULL, created_by varchar(255) NOT NULL, created_on datetime2 NOT NULL, description varchar(512), modified_by varchar(255) NOT NULL, modified_on datetime2 NOT NULL, name varchar(255) NOT NULL)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-9::apiman (generated)
CREATE TABLE pd_templates (policydef_id varchar(255) NOT NULL, language varchar(255), template varchar(2048))
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-10::apiman (generated)
CREATE TABLE permissions (role_id varchar(255) NOT NULL, permissions int)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-11::apiman (generated)
CREATE TABLE plan_versions (id bigint NOT NULL, created_by varchar(255) NOT NULL, created_on datetime2 NOT NULL, locked_on datetime2, modified_by varchar(255) NOT NULL, modified_on datetime2 NOT NULL, status varchar(255) NOT NULL, version varchar(255) NOT NULL, plan_id varchar(255), plan_org_id varchar(255))
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-12::apiman (generated)
CREATE TABLE plans (id varchar(255) NOT NULL, created_by varchar(255) NOT NULL, created_on datetime2 NOT NULL, description varchar(512), name varchar(255) NOT NULL, organization_id varchar(255) NOT NULL)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-13::apiman (generated)
CREATE TABLE plugins (id bigint NOT NULL, artifact_id varchar(255) NOT NULL, classifier varchar(255), created_by varchar(255) NOT NULL, created_on datetime2 NOT NULL, description varchar(512), group_id varchar(255) NOT NULL, name varchar(255) NOT NULL, type varchar(255), version varchar(255) NOT NULL, deleted bit)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-14::apiman (generated)
CREATE TABLE policies (id bigint NOT NULL, configuration varchar(MAX), created_by varchar(255) NOT NULL, created_on datetime2 NOT NULL, entity_id varchar(255) NOT NULL, entity_version varchar(255) NOT NULL, modified_by varchar(255) NOT NULL, modified_on datetime2 NOT NULL, name varchar(255) NOT NULL, order_index int NOT NULL, organization_id varchar(255) NOT NULL, type varchar(255) NOT NULL, definition_id varchar(255) NOT NULL)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-15::apiman (generated)
CREATE TABLE policydefs (id varchar(255) NOT NULL, description varchar(512) NOT NULL, form varchar(255), form_type varchar(255), icon varchar(255) NOT NULL, name varchar(255) NOT NULL, plugin_id bigint, policy_impl varchar(255) NOT NULL, deleted bit)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-16::apiman (generated)
CREATE TABLE roles (id varchar(255) NOT NULL, auto_grant bit, created_by varchar(255) NOT NULL, created_on datetime2 NOT NULL, description varchar(512), name varchar(255))
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-17::apiman (generated)
CREATE TABLE api_defs (id bigint NOT NULL, data varbinary(MAX), api_version_id bigint)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-18::apiman (generated)
CREATE TABLE api_versions (id bigint NOT NULL, created_by varchar(255) NOT NULL, created_on datetime2 NOT NULL, definition_type varchar(255), endpoint varchar(255), endpoint_type varchar(255), endpoint_ct varchar(255), modified_by varchar(255) NOT NULL, modified_on datetime2 NOT NULL, public_api bit NOT NULL, parse_payload bit, strip_keys bit, published_on datetime2, retired_on datetime2, status varchar(255) NOT NULL, version varchar(255), api_id varchar(255), api_org_id varchar(255), definition_url varchar(255))
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-19::apiman (generated)
CREATE TABLE apis (id varchar(255) NOT NULL, created_by varchar(255) NOT NULL, created_on datetime2 NOT NULL, description varchar(512), name varchar(255) NOT NULL, organization_id varchar(255) NOT NULL, num_published int)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-20::apiman (generated)
CREATE TABLE api_gateways (api_version_id bigint NOT NULL, gateway_id varchar(255) NOT NULL)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-21::apiman (generated)
CREATE TABLE api_plans (api_version_id bigint NOT NULL, plan_id varchar(255) NOT NULL, version varchar(255) NOT NULL)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-22::apiman (generated)
CREATE TABLE users (username varchar(255) NOT NULL, email varchar(255), full_name varchar(255), joined_on datetime2)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-23::apiman (generated)
CREATE TABLE downloads (id varchar(255) NOT NULL, type varchar(255), path varchar(255), expires datetime2)
GO

-- Changeset src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-24::apiman (generated)
CREATE TABLE metadata (id bigint NOT NULL, exported_on datetime2, imported_on datetime2, apiman_version varchar(255), apiman_version_at_import varchar(255), success bit)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-23::apiman (generated)
ALTER TABLE endpoint_properties ADD PRIMARY KEY (api_version_id, name)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-24::apiman (generated)
ALTER TABLE api_gateways ADD PRIMARY KEY (api_version_id, gateway_id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-25::apiman (generated)
ALTER TABLE api_plans ADD PRIMARY KEY (api_version_id, plan_id, version)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-26::apiman (generated)
ALTER TABLE client_versions ADD CONSTRAINT client_versionsPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-27::apiman (generated)
ALTER TABLE clients ADD CONSTRAINT clientsPK PRIMARY KEY (id, organization_id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-28::apiman (generated)
ALTER TABLE auditlog ADD CONSTRAINT auditlogPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-29::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT contractsPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-30::apiman (generated)
ALTER TABLE gateways ADD CONSTRAINT gatewaysPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-31::apiman (generated)
ALTER TABLE memberships ADD CONSTRAINT membershipsPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-32::apiman (generated)
ALTER TABLE organizations ADD CONSTRAINT organizationsPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-33::apiman (generated)
ALTER TABLE plan_versions ADD CONSTRAINT plan_versionsPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-34::apiman (generated)
ALTER TABLE plans ADD CONSTRAINT plansPK PRIMARY KEY (id, organization_id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-35::apiman (generated)
ALTER TABLE plugins ADD CONSTRAINT pluginsPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-36::apiman (generated)
ALTER TABLE policies ADD CONSTRAINT policiesPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-37::apiman (generated)
ALTER TABLE policydefs ADD CONSTRAINT policydefsPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-38::apiman (generated)
ALTER TABLE roles ADD CONSTRAINT rolesPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-39::apiman (generated)
ALTER TABLE api_defs ADD CONSTRAINT api_defsPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-40::apiman (generated)
ALTER TABLE api_versions ADD CONSTRAINT api_versionsPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-41::apiman (generated)
ALTER TABLE apis ADD CONSTRAINT apisPK PRIMARY KEY (id, organization_id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-42::apiman (generated)
ALTER TABLE users ADD CONSTRAINT usersPK PRIMARY KEY (username)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-43::apiman (generated)
ALTER TABLE apis ADD CONSTRAINT FK_31hj3xmhp1wedxjh5bklnlg15 FOREIGN KEY (organization_id) REFERENCES organizations (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-44::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT FK_6h06sgs4dudh1wehmk0us973g FOREIGN KEY (clientv_id) REFERENCES client_versions (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-45::apiman (generated)
ALTER TABLE api_defs ADD CONSTRAINT FK_81fuw1n8afmvpw4buk7l4tyxk FOREIGN KEY (api_version_id) REFERENCES api_versions (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-46::apiman (generated)
ALTER TABLE client_versions ADD CONSTRAINT FK_8epnoby31bt7xakegakigpikp FOREIGN KEY (client_id, client_org_id) REFERENCES clients (id, organization_id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-47::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT FK_8o6t1f3kg96rxy5uv51f6k9fy FOREIGN KEY (apiv_id) REFERENCES api_versions (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-48::apiman (generated)
ALTER TABLE api_versions ADD CONSTRAINT FK_92erjg9k1lni97gd87nt6tq37 FOREIGN KEY (api_id, api_org_id) REFERENCES apis (id, organization_id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-49::apiman (generated)
ALTER TABLE endpoint_properties ADD CONSTRAINT FK_gn0ydqur10sxuvpyw2jvv4xxb FOREIGN KEY (api_version_id) REFERENCES api_versions (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-50::apiman (generated)
ALTER TABLE clients ADD CONSTRAINT FK_jenpu34rtuncsgvtw0sfo8qq9 FOREIGN KEY (organization_id) REFERENCES organizations (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-51::apiman (generated)
ALTER TABLE policies ADD CONSTRAINT FK_l4q6we1bos1yl9unmogei6aja FOREIGN KEY (definition_id) REFERENCES policydefs (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-52::apiman (generated)
ALTER TABLE plans ADD CONSTRAINT FK_lwhc7xrdbsun1ak2uvfu0prj8 FOREIGN KEY (organization_id) REFERENCES organizations (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-53::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT FK_nyw8xu6m8cx4rwwbtrxbjneui FOREIGN KEY (planv_id) REFERENCES plan_versions (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-54::apiman (generated)
ALTER TABLE api_gateways ADD CONSTRAINT FK_p5dm3cngljt6yrsnvc7uc6a75 FOREIGN KEY (api_version_id) REFERENCES api_versions (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-55::apiman (generated)
ALTER TABLE pd_templates ADD CONSTRAINT FK_prbnn7j7m6m3pxt2dsn9gwlw8 FOREIGN KEY (policydef_id) REFERENCES policydefs (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-56::apiman (generated)
ALTER TABLE permissions ADD CONSTRAINT FK_sq51ihfrapwdr98uufenhcocg FOREIGN KEY (role_id) REFERENCES roles (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-57::apiman (generated)
ALTER TABLE api_plans ADD CONSTRAINT FK_t7uvfcsswopb9kh8wpa86blqr FOREIGN KEY (api_version_id) REFERENCES api_versions (id)
GO

-- Changeset src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-58::apiman (generated)
ALTER TABLE plan_versions ADD CONSTRAINT FK_tonylvm2ypnq3efxqr1g0m9fs FOREIGN KEY (plan_id, plan_org_id) REFERENCES plans (id, organization_id)
GO

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-1::apiman
ALTER TABLE plugins ADD CONSTRAINT UK_plugins_1 UNIQUE (group_id, artifact_id)
GO

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-2::apiman
ALTER TABLE memberships ADD CONSTRAINT UK_memberships_1 UNIQUE (user_id, role_id, org_id)
GO

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-3::apiman
ALTER TABLE plan_versions ADD CONSTRAINT UK_plan_versions_1 UNIQUE (plan_id, plan_org_id, version)
GO

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-4::apiman
ALTER TABLE client_versions ADD CONSTRAINT UK_client_versions_1 UNIQUE (client_id, client_org_id, version)
GO

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-5::apiman
ALTER TABLE api_versions ADD CONSTRAINT UK_api_versions_1 UNIQUE (api_id, api_org_id, version)
GO

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-6::apiman
ALTER TABLE api_defs ADD CONSTRAINT UK_api_defs_1 UNIQUE (api_version_id)
GO

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-7::apiman
ALTER TABLE contracts ADD CONSTRAINT UK_contracts_1 UNIQUE (clientv_id, apiv_id)
GO

-- Changeset src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-8::apiman
ALTER TABLE client_versions ADD CONSTRAINT UK_client_versions_2 UNIQUE (apikey)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-1::apiman
CREATE NONCLUSTERED INDEX IDX_auditlog_1 ON auditlog(who)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-2::apiman
CREATE NONCLUSTERED INDEX IDX_auditlog_2 ON auditlog(organization_id, entity_id, entity_version, entity_type)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-3::apiman
CREATE NONCLUSTERED INDEX IDX_FK_pd_templates_1 ON pd_templates(policydef_id)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-4::apiman
CREATE NONCLUSTERED INDEX IDX_users_1 ON users(username)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-5::apiman
CREATE NONCLUSTERED INDEX IDX_users_2 ON users(full_name)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-6::apiman
CREATE NONCLUSTERED INDEX IDX_FK_permissions_1 ON permissions(role_id)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-7::apiman
CREATE NONCLUSTERED INDEX IDX_memberships_1 ON memberships(user_id)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-8::apiman
CREATE NONCLUSTERED INDEX IDX_organizations_1 ON organizations(name)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-9::apiman
CREATE NONCLUSTERED INDEX IDX_FK_plans_1 ON plans(organization_id)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-10::apiman
CREATE NONCLUSTERED INDEX IDX_FK_clients_1 ON clients(organization_id)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-11::apiman
CREATE NONCLUSTERED INDEX IDX_apis_1 ON apis(name)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-12::apiman
CREATE NONCLUSTERED INDEX IDX_FK_apis_1 ON apis(organization_id)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-13::apiman
CREATE NONCLUSTERED INDEX IDX_policies_1 ON policies(organization_id, entity_id, entity_version)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-14::apiman
CREATE NONCLUSTERED INDEX IDX_policies_2 ON policies(order_index)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-15::apiman
CREATE NONCLUSTERED INDEX IDX_FK_policies_1 ON policies(definition_id)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-16::apiman
CREATE NONCLUSTERED INDEX IDX_FK_contracts_p ON contracts(planv_id)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-17::apiman
CREATE NONCLUSTERED INDEX IDX_FK_contracts_s ON contracts(apiv_id)
GO

-- Changeset src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-18::apiman
CREATE NONCLUSTERED INDEX IDX_FK_contracts_a ON contracts(clientv_id)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-6::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE developer_mappings (developer_id varchar(255) NOT NULL, client_id varchar(255) NOT NULL, organization_id varchar(255) NOT NULL, CONSTRAINT PK_DEVELOPER_MAPPINGS PRIMARY KEY (developer_id, client_id, organization_id))
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-7::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE developers (id varchar(255) NOT NULL, CONSTRAINT developersPK PRIMARY KEY (id))
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-8::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE notification_category_preferences (NotificationPreferenceEntity_id bigint NOT NULL, category varchar(255))
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-9::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE notification_preferences (id bigint IDENTITY (1, 1) NOT NULL, type varchar(255) NOT NULL, user_id varchar(255) NOT NULL, CONSTRAINT notification_preferencesPK PRIMARY KEY (id))
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-10::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE notification_types (type varchar(255) NOT NULL, description varchar(255) NOT NULL, CONSTRAINT notification_typesPK PRIMARY KEY (type))
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-11::msavy marc@blackparrotlabs.io (generated)
CREATE TABLE notifications (id bigint IDENTITY (1, 1) NOT NULL, category varchar(255) NOT NULL, created_on datetime2, modified_on datetime2, payload JSON NOT NULL, reason varchar(255) NOT NULL, reason_message varchar(255) NOT NULL, recipient varchar(255) NOT NULL, source varchar(255) NOT NULL, status varchar(255) NOT NULL, CONSTRAINT notificationsPK PRIMARY KEY (id))
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-14::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE api_versions ADD extended_description nvarchar(MAX)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-15::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE apis ADD image_file_ref varchar(255)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-16::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE clients ADD image_file_ref varchar(255)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-17::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE api_plans ADD requires_approval bit NOT NULL
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-18::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE contracts ADD status int NOT NULL
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-19::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE downloads ADD CONSTRAINT downloadsPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-20::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE metadata ADD CONSTRAINT metadataPK PRIMARY KEY (id)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-21::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE plan_versions ADD CONSTRAINT UK53ss55b2hiye2e9v6lftdo5jg UNIQUE (plan_id, plan_org_id, version)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-22::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE memberships ADD CONSTRAINT UKfwy6c61bcnvshcsxubanjvi08 UNIQUE (user_id, role_id, org_id)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-23::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE plugins ADD CONSTRAINT UKofbok9ushig9vviq01dnu11x UNIQUE (group_id, artifact_id)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-24::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE notification_preferences ADD CONSTRAINT UserAllowedOnlyOneOfEachNotificationType UNIQUE (user_id, type)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-26::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE notification_category_preferences ADD CONSTRAINT FKaq4x0n83d83xevui0ctqwdgbi FOREIGN KEY (NotificationPreferenceEntity_id) REFERENCES notification_preferences (id)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::dev-portal-2-initial-changeset-27::msavy marc@blackparrotlabs.io (generated)
ALTER TABLE developer_mappings ADD CONSTRAINT FKhl2dwc4m0kvisedxfb9crceqd FOREIGN KEY (developer_id) REFERENCES developers (id)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633441143380-4::msavy (generated)
CREATE TABLE kv_tags (id bigint IDENTITY (1, 1) NOT NULL, [key] varchar(255) NOT NULL, value varchar(255), CONSTRAINT kv_tagsPK PRIMARY KEY (id))
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633441143380-5::msavy (generated)
CREATE TABLE api_tag (api_id varchar(255) NOT NULL, org_id varchar(255) NOT NULL, tag_id bigint NOT NULL, CONSTRAINT PK_API_TAG PRIMARY KEY (api_id, org_id, tag_id))
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-6::msavy (generated)
CREATE TABLE blob_store (id varchar(255) NOT NULL, mrblobby varbinary(MAX) NOT NULL, created_on datetime2 NOT NULL, hash bigint NOT NULL, mime_type varchar(255) NOT NULL, modified_on datetime2 NOT NULL, name varchar(255) NOT NULL, ref_count int NOT NULL, CONSTRAINT blob_storePK PRIMARY KEY (id))
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-7::msavy (generated)
CREATE TABLE outbox (id bigint IDENTITY (1, 1) NOT NULL, event_version bigint NOT NULL, payload JSON NOT NULL, source varchar(255) NOT NULL, subject varchar(255) NOT NULL, time datetime2 NOT NULL, type varchar(255) NOT NULL, CONSTRAINT outboxPK PRIMARY KEY (id))
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-8::msavy (generated)
ALTER TABLE blob_store ADD CONSTRAINT UC_BLOB_STOREID_COL UNIQUE (id)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-10::msavy (generated)
ALTER TABLE blob_store ADD CONSTRAINT UK_4jee67ekw7s4y8spoc58i4dsf UNIQUE (hash, mime_type, name)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-15::msavy (generated)
ALTER TABLE api_tag ADD CONSTRAINT FK2h64maqscweorti1hta9josl2 FOREIGN KEY (tag_id) REFERENCES kv_tags (id)
GO

-- Changeset src/main/liquibase/current/20211002-154432-apiman3-dev-portal-2-initial.changelog.xml::1633542267834-16::msavy (generated)
ALTER TABLE api_tag ADD CONSTRAINT FKlpr8yu65omneju5297uqthb6k FOREIGN KEY (api_id, org_id) REFERENCES apis (id, organization_id)
GO

-- Changeset src/main/liquibase/current/20211206-add-locale-to-user-profile.xml::add-locale-to-use-profile::msavy marc@blackparrotlabs.io (manual changeset)
ALTER TABLE users ADD locale varchar(255)
GO

-- Changeset src/main/liquibase/current/20220228-rework-notification-filtering.xml::1646057700977-6::msavy (generated)
CREATE TABLE notification_rules (NotificationPreferenceEntity_id bigint NOT NULL, enabled bit, expression varchar(255), message varchar(255), source varchar(255))
GO

-- Changeset src/main/liquibase/current/20220228-rework-notification-filtering.xml::1646057700977-7::msavy (generated)
ALTER TABLE notification_rules ADD CONSTRAINT FKbxdud6qk8e28eq1mjihqauybo FOREIGN KEY (NotificationPreferenceEntity_id) REFERENCES notification_preferences (id)
GO

-- Changeset src/main/liquibase/current/20220228-rework-notification-filtering.xml::1646057700977-8::msavy (generated)
ALTER TABLE notification_category_preferences DROP CONSTRAINT FKaq4x0n83d83xevui0ctqwdgbi
GO

-- Changeset src/main/liquibase/current/20220228-rework-notification-filtering.xml::1646057700977-10::msavy (generated)
DROP TABLE notification_category_preferences
GO

-- Changeset src/main/liquibase/current/20220228-rework-notification-filtering.xml::1646057700977-11::msavy (generated)
DROP TABLE notification_types
GO

-- Changeset src/main/liquibase/current/20220228-rework-notification-filtering.xml::1646232783603-7::msavy (generated)
ALTER TABLE notification_preferences ADD CONSTRAINT FKt9qjvmcl36i14utm5uptyqg84 FOREIGN KEY (user_id) REFERENCES users (username)
GO

-- Changeset src/main/liquibase/current/20220330-discoverability.xml::1647015740776-8::msavy (generated)
ALTER TABLE api_plans ADD discoverability varchar(255) CONSTRAINT DF_api_plans_discoverability DEFAULT 'ORG_MEMBERS'
GO

ALTER TABLE api_versions ADD discoverability varchar(255) CONSTRAINT DF_api_versions_discoverability DEFAULT 'ORG_MEMBERS'
GO

-- Changeset src/main/liquibase/current/20220330-discoverability.xml::1646489262610-4::msavy (generated)
CREATE TABLE discoverability (id varchar(255) NOT NULL, org_id varchar(255), api_id varchar(255), api_version varchar(255), plan_id varchar(255), plan_version varchar(255), discoverability varchar(255), CONSTRAINT discoverabilityPK PRIMARY KEY (id))
GO

CREATE NONCLUSTERED INDEX api_plan_discoverability_index ON discoverability(org_id, api_id, api_version, plan_id, plan_version)
GO

CREATE NONCLUSTERED INDEX api_version_discoverability_index ON discoverability(org_id, api_id, api_version)
GO

-- Changeset src/main/liquibase/current/20220330-discoverability.xml::discoverability-view-trigger::msavy
-- A hand-rolled materialized view that synchronises changes to 'discoverability' on `api_plans` and `api_versions` to `discoverability`
--             This enables very efficient search without performing multiple joins, plus avoids significantly complicating queries by having to
--             reference all different locations that `discoverability` can be set.
--
--             Plausibly we may need to add additional location(s) in future such as `organization`, which should be mostly copy-and-paste.
--
--             A nice alternative to this would be CDC with something like Debezium, but that requires the DB to have been set up properly (sometimes
--             including special plugins), which makes the deployment more complicated and difficult.
--
--             Materialized views were considered, but these have extremely variable functionality on different DBs. For example, on Postgres all
--             materialized views must be manually updated using a special SQL command. There is no baked-in commit or time-based refresh.
-- ApiPlan
CREATE TRIGGER insert_apiplan_into_discoverability
    ON api_plans AFTER INSERT
    AS
BEGIN
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS
    (
        SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
        FROM api_versions av, inserted
        WHERE av.id = inserted.api_version_id
    )
    INSERT INTO discoverability(id, org_id, api_id, api_version, plan_id, plan_version, discoverability)
    SELECT
        CONCAT_WS(':',
            Api_Version_CTE.api_org_id,
            Api_Version_CTE.api_id,
            Api_Version_CTE.api_version,
            inserted.plan_id,
            inserted.version
        ),
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        inserted.plan_id,
        inserted.version,
        inserted.discoverability
    FROM Api_Version_CTE, inserted
GO

END
GO

CREATE TRIGGER update_apiplan_into_discoverability
    ON api_plans AFTER UPDATE
    AS
BEGIN
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS (
        SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
        FROM api_versions av, inserted
        WHERE av.id = inserted.api_version_id
    )
    UPDATE discoverability
    SET org_id = Api_Version_CTE.api_org_id,
        api_id = Api_Version_CTE.api_id,
        api_version = Api_Version_CTE.api_version,
        plan_id = inserted.plan_id,
        plan_version = inserted.version,
        discoverability = inserted.discoverability
    FROM Api_Version_CTE, discoverability, inserted
    WHERE id = CONCAT_WS(':',
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        inserted.plan_id,
        inserted.version
    )
GO

END
GO

CREATE TRIGGER api_plan_discoverability_trigger_delete
    ON api_plans AFTER DELETE
    AS
BEGIN
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS
    (
        SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
        FROM api_versions av, deleted
        WHERE av.id = deleted.api_version_id
    )
    DELETE d
    FROM discoverability d, Api_Version_CTE, deleted
    WHERE d.id = CONCAT_WS(':',
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        deleted.plan_id,
        deleted.version
    )
GO

END
GO

-- ApiVersion
CREATE TRIGGER insert_apiversion_into_discoverability
    ON api_versions AFTER INSERT
    AS
BEGIN
    INSERT INTO discoverability(id, org_id, api_id, api_version, plan_id, plan_version, discoverability)
    SELECT
        CONCAT_WS(':', inserted.api_org_id, inserted.api_id, inserted.version),
        inserted.api_org_id,
        inserted.api_id,
        inserted.version,
        NULL,
        NULL,
        inserted.discoverability
    FROM inserted
END
GO

CREATE TRIGGER update_apiversion_into_discoverability
    ON api_versions AFTER INSERT
    AS
BEGIN
    UPDATE discoverability
    SET org_id = inserted.api_org_id,
        api_id = inserted.api_id,
        api_version = inserted.version,
        plan_id = NULL,
        plan_version = NULL,
        discoverability = inserted.discoverability
    FROM inserted
    WHERE id = CONCAT_WS(':', inserted.api_org_id, inserted.api_id, inserted.version)
GO

END
GO

CREATE TRIGGER delete_apiversion_from_discoverability
    ON api_versions AFTER DELETE
    AS
BEGIN
    DELETE d
    FROM discoverability d, deleted
    WHERE d.id = CONCAT_WS(':', deleted.api_org_id, deleted.api_id, deleted.version)
GO

END
GO

