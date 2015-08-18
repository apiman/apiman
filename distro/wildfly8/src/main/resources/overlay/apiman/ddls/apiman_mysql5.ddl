--  *********************************************************************
--  Update Database Script
--  *********************************************************************
--  Change Log: c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/master.xml
--  Ran at: 7/21/15 11:22 AM
--  Against: null@offline:mysql?version=5.1&caseSensitive=false&changeLogFile=c:\Users\ewittman\git\apiman\apiman\distro\ddl/target/changelog/mysql/databasechangelog.csv
--  Liquibase version: 3.3.5
--  *********************************************************************

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/000-apiman-manager-api.db.sequences.changelog.xml::1434723514712-1::apiman
CREATE TABLE `hibernate_sequence` (`next_val` bigint(20) DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `hibernate_sequence` VALUES (999);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-1::apiman (generated)
CREATE TABLE application_versions (id BIGINT NOT NULL, created_by VARCHAR(255) NOT NULL, created_on datetime NOT NULL, modified_by VARCHAR(255) NOT NULL, modified_on datetime NOT NULL, published_on datetime NULL, retired_on datetime NULL, status VARCHAR(255) NOT NULL, version VARCHAR(255) NOT NULL, app_id VARCHAR(255) NULL, app_org_id VARCHAR(255) NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-2::apiman (generated)
CREATE TABLE applications (id VARCHAR(255) NOT NULL, created_by VARCHAR(255) NOT NULL, created_on datetime NOT NULL, description VARCHAR(512) NULL, name VARCHAR(255) NOT NULL, organization_id VARCHAR(255) NOT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-3::apiman (generated)
CREATE TABLE auditlog (id BIGINT NOT NULL, created_on datetime NOT NULL, data LONGTEXT NULL, entity_id VARCHAR(255) NULL, entity_type VARCHAR(255) NOT NULL, entity_version VARCHAR(255) NULL, organization_id VARCHAR(255) NOT NULL, what VARCHAR(255) NOT NULL, who VARCHAR(255) NOT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-4::apiman (generated)
CREATE TABLE contracts (id BIGINT NOT NULL, apikey VARCHAR(255) NOT NULL, created_by VARCHAR(255) NOT NULL, created_on datetime NOT NULL, appv_id BIGINT NULL, planv_id BIGINT NULL, svcv_id BIGINT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-5::apiman (generated)
CREATE TABLE endpoint_properties (service_version_id BIGINT NOT NULL, value VARCHAR(255) NULL, name VARCHAR(255) NOT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-6::apiman (generated)
CREATE TABLE gateways (id VARCHAR(255) NOT NULL, configuration LONGTEXT NOT NULL, created_by VARCHAR(255) NOT NULL, created_on datetime NOT NULL, description VARCHAR(512) NULL, modified_by VARCHAR(255) NOT NULL, modified_on datetime NOT NULL, name VARCHAR(255) NOT NULL, type VARCHAR(255) NOT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-7::apiman (generated)
CREATE TABLE memberships (id BIGINT NOT NULL, created_on datetime NULL, org_id VARCHAR(255) NULL, role_id VARCHAR(255) NULL, user_id VARCHAR(255) NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-8::apiman (generated)
CREATE TABLE organizations (id VARCHAR(255) NOT NULL, created_by VARCHAR(255) NOT NULL, created_on datetime NOT NULL, description VARCHAR(512) NULL, modified_by VARCHAR(255) NOT NULL, modified_on datetime NOT NULL, name VARCHAR(255) NOT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-9::apiman (generated)
CREATE TABLE pd_templates (policydef_id VARCHAR(255) NOT NULL, language VARCHAR(255) NULL, template VARCHAR(2048) NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-10::apiman (generated)
CREATE TABLE permissions (role_id VARCHAR(255) NOT NULL, permissions INT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-11::apiman (generated)
CREATE TABLE plan_versions (id BIGINT NOT NULL, created_by VARCHAR(255) NOT NULL, created_on datetime NOT NULL, locked_on datetime NULL, modified_by VARCHAR(255) NOT NULL, modified_on datetime NOT NULL, status VARCHAR(255) NOT NULL, version VARCHAR(255) NOT NULL, plan_id VARCHAR(255) NULL, plan_org_id VARCHAR(255) NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-12::apiman (generated)
CREATE TABLE plans (id VARCHAR(255) NOT NULL, created_by VARCHAR(255) NOT NULL, created_on datetime NOT NULL, description VARCHAR(512) NULL, name VARCHAR(255) NOT NULL, organization_id VARCHAR(255) NOT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-13::apiman (generated)
CREATE TABLE plugins (id BIGINT NOT NULL, artifact_id VARCHAR(255) NOT NULL, classifier VARCHAR(255) NULL, created_by VARCHAR(255) NOT NULL, created_on datetime NOT NULL, description VARCHAR(512) NULL, group_id VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL, type VARCHAR(255) NULL, version VARCHAR(255) NOT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-14::apiman (generated)
CREATE TABLE policies (id BIGINT NOT NULL, configuration LONGTEXT NULL, created_by VARCHAR(255) NOT NULL, created_on datetime NOT NULL, entity_id VARCHAR(255) NOT NULL, entity_version VARCHAR(255) NOT NULL, modified_by VARCHAR(255) NOT NULL, modified_on datetime NOT NULL, name VARCHAR(255) NOT NULL, order_index INT NOT NULL, organization_id VARCHAR(255) NOT NULL, type VARCHAR(255) NOT NULL, definition_id VARCHAR(255) NOT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-15::apiman (generated)
CREATE TABLE policydefs (id VARCHAR(255) NOT NULL, description VARCHAR(512) NOT NULL, form VARCHAR(255) NULL, form_type VARCHAR(255) NULL, icon VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL, plugin_id BIGINT NULL, policy_impl VARCHAR(255) NOT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-16::apiman (generated)
CREATE TABLE roles (id VARCHAR(255) NOT NULL, auto_grant BIT(1) NULL, created_by VARCHAR(255) NOT NULL, created_on datetime NOT NULL, description VARCHAR(512) NULL, name VARCHAR(255) NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-17::apiman (generated)
CREATE TABLE service_defs (id BIGINT NOT NULL, data LONGBLOB NULL, service_version_id BIGINT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-18::apiman (generated)
CREATE TABLE service_versions (id BIGINT NOT NULL, created_by VARCHAR(255) NOT NULL, created_on datetime NOT NULL, definition_type VARCHAR(255) NULL, endpoint VARCHAR(255) NULL, endpoint_type VARCHAR(255) NULL, modified_by VARCHAR(255) NOT NULL, modified_on datetime NOT NULL, public_service BIT(1) NOT NULL, published_on datetime NULL, retired_on datetime NULL, status VARCHAR(255) NOT NULL, version VARCHAR(255) NULL, service_id VARCHAR(255) NULL, service_org_id VARCHAR(255) NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-19::apiman (generated)
CREATE TABLE services (id VARCHAR(255) NOT NULL, created_by VARCHAR(255) NOT NULL, created_on datetime NOT NULL, description VARCHAR(512) NULL, name VARCHAR(255) NOT NULL, organization_id VARCHAR(255) NOT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-20::apiman (generated)
CREATE TABLE svc_gateways (service_version_id BIGINT NOT NULL, gateway_id VARCHAR(255) NOT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-21::apiman (generated)
CREATE TABLE svc_plans (service_version_id BIGINT NOT NULL, plan_id VARCHAR(255) NOT NULL, version VARCHAR(255) NOT NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/010-apiman-manager-api.db.tables.changelog.xml::1436469846462-22::apiman (generated)
CREATE TABLE users (username VARCHAR(255) NOT NULL, email VARCHAR(255) NULL, full_name VARCHAR(255) NULL, joined_on datetime NULL);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/050-apiman-manager-api.db.data.changelog.xml::1434686531709-1::apiman
INSERT INTO gateways (id, configuration, created_by, created_on, description, modified_by, modified_on, name, type) VALUES ('TheGateway', '$CRYPT::PmrNC1m25oGSO8fC3XnxKSepmQsbxEZCldn+hYi9bQ10m8m4tEOHYU7gz5w2hcY2cMRPB3Rw+4FZYoeX0n3qEvyk+2Yf+ym3nKw3UtmtHViHLibBzWPY+8OTtJlZVb8dA8yd0TMkiwqk1WGHLSbyjmQujZ07RBK9wkwDahYwQEw=', 'admin', '2015-06-18 17:56:58.083', 'This is the gateway.', 'admin', '2015-06-18 17:56:58.083', 'The Gateway', 'REST');

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/050-apiman-manager-api.db.data.changelog.xml::1434686531709-2::apiman
INSERT INTO pd_templates (policydef_id, language, template) VALUES ('IPWhitelistPolicy', NULL, 'Only requests that originate from the set of @{ipList.size()} configured IP address(es) will be allowed to invoke the managed service.');

INSERT INTO pd_templates (policydef_id, language, template) VALUES ('IPBlacklistPolicy', NULL, 'Requests that originate from the set of @{ipList.size()} configured IP address(es) will be denied access to the managed service.');

INSERT INTO pd_templates (policydef_id, language, template) VALUES ('BASICAuthenticationPolicy', NULL, 'Access to the service is protected by BASIC Authentication through the ''@{realm}'' authentication realm.  @if{forwardIdentityHttpHeader != null}Successfully authenticated requests will forward the authenticated identity to the back end service via the ''@{forwardIdentityHttpHeader}'' custom HTTP header.@end{}');

INSERT INTO pd_templates (policydef_id, language, template) VALUES ('RateLimitingPolicy', NULL, 'Consumers are limited to @{limit} requests per @{granularity} per @{period}.');

INSERT INTO pd_templates (policydef_id, language, template) VALUES ('QuotaPolicy', NULL, 'Consumers cannot exceed their quota of @{limit} requests per @{granularity} per @{period}.');

INSERT INTO pd_templates (policydef_id, language, template) VALUES ('TransferQuotaPolicy', NULL, 'Consumers are limited to transferring @{limit} bytes per per @{granularity} per @{period}.');

INSERT INTO pd_templates (policydef_id, language, template) VALUES ('IgnoredResourcesPolicy', NULL, 'Requests matching any of the @{pathsToIgnore.size()} regular expressions provided will receive a 404 error code.');

INSERT INTO pd_templates (policydef_id, language, template) VALUES ('URLRewritingPolicy', NULL, 'Responses will be modified by finding all text matching regular expression ''@{fromRegex}'' with ''@{toReplacement}''.');

INSERT INTO pd_templates (policydef_id, language, template) VALUES ('CachingPolicy', NULL, 'API responses will be cached for @{ttl} seconds.');

INSERT INTO pd_templates (policydef_id, language, template) VALUES ('AuthorizationPolicy', NULL, 'Appropriate authorization roles are required.  There are @{rules.size()} authorization rules defined.');

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/050-apiman-manager-api.db.data.changelog.xml::1434686531709-3::apiman
INSERT INTO permissions (role_id, permissions) VALUES ('OrganizationOwner', 1);

INSERT INTO permissions (role_id, permissions) VALUES ('OrganizationOwner', 2);

INSERT INTO permissions (role_id, permissions) VALUES ('OrganizationOwner', 3);

INSERT INTO permissions (role_id, permissions) VALUES ('OrganizationOwner', 6);

INSERT INTO permissions (role_id, permissions) VALUES ('OrganizationOwner', 8);

INSERT INTO permissions (role_id, permissions) VALUES ('OrganizationOwner', 5);

INSERT INTO permissions (role_id, permissions) VALUES ('OrganizationOwner', 9);

INSERT INTO permissions (role_id, permissions) VALUES ('OrganizationOwner', 11);

INSERT INTO permissions (role_id, permissions) VALUES ('OrganizationOwner', 7);

INSERT INTO permissions (role_id, permissions) VALUES ('OrganizationOwner', 4);

INSERT INTO permissions (role_id, permissions) VALUES ('OrganizationOwner', 10);

INSERT INTO permissions (role_id, permissions) VALUES ('OrganizationOwner', 0);

INSERT INTO permissions (role_id, permissions) VALUES ('ApplicationDeveloper', 6);

INSERT INTO permissions (role_id, permissions) VALUES ('ApplicationDeveloper', 8);

INSERT INTO permissions (role_id, permissions) VALUES ('ApplicationDeveloper', 7);

INSERT INTO permissions (role_id, permissions) VALUES ('ServiceDeveloper', 3);

INSERT INTO permissions (role_id, permissions) VALUES ('ServiceDeveloper', 5);

INSERT INTO permissions (role_id, permissions) VALUES ('ServiceDeveloper', 9);

INSERT INTO permissions (role_id, permissions) VALUES ('ServiceDeveloper', 11);

INSERT INTO permissions (role_id, permissions) VALUES ('ServiceDeveloper', 4);

INSERT INTO permissions (role_id, permissions) VALUES ('ServiceDeveloper', 10);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/050-apiman-manager-api.db.data.changelog.xml::1434686531709-4::apiman
INSERT INTO policydefs (id, description, form, form_type, icon, name, plugin_id, policy_impl) VALUES ('IPWhitelistPolicy', 'Only requests that originate from a specified set of valid IP addresses will be allowed through.', NULL, 'Default', 'filter', 'IP Whitelist Policy', NULL, 'class:io.apiman.gateway.engine.policies.IPWhitelistPolicy');

INSERT INTO policydefs (id, description, form, form_type, icon, name, plugin_id, policy_impl) VALUES ('IPBlacklistPolicy', 'Requests that originate from a specified set of valid IP addresses will be denied access.', NULL, 'Default', 'thumbs-down', 'IP Blacklist Policy', NULL, 'class:io.apiman.gateway.engine.policies.IPBlacklistPolicy');

INSERT INTO policydefs (id, description, form, form_type, icon, name, plugin_id, policy_impl) VALUES ('BASICAuthenticationPolicy', 'Enables HTTP BASIC Authentication on a service.  Some configuration required.', NULL, 'Default', 'lock', 'BASIC Authentication Policy', NULL, 'class:io.apiman.gateway.engine.policies.BasicAuthenticationPolicy');

INSERT INTO policydefs (id, description, form, form_type, icon, name, plugin_id, policy_impl) VALUES ('RateLimitingPolicy', 'Enforces rate configurable request rate limits on a service.  This ensures that consumers can''t overload a service with too many requests.', NULL, 'Default', 'tachometer', 'Rate Limiting Policy', NULL, 'class:io.apiman.gateway.engine.policies.RateLimitingPolicy');

INSERT INTO policydefs (id, description, form, form_type, icon, name, plugin_id, policy_impl) VALUES ('QuotaPolicy', 'Provides a way to limit the total number of requests that can be sent to an API.', NULL, 'Default', 'exchange', 'Quota Policy', NULL, 'class:io.apiman.gateway.engine.policies.QuotaPolicy');

INSERT INTO policydefs (id, description, form, form_type, icon, name, plugin_id, policy_impl) VALUES ('TransferQuotaPolicy', 'Provides a way to limit the total number of bytes that can be transferred from (or to) an API.', NULL, 'Default', 'download', 'Transfer Quota Policy', NULL, 'class:io.apiman.gateway.engine.policies.TransferQuotaPolicy');

INSERT INTO policydefs (id, description, form, form_type, icon, name, plugin_id, policy_impl) VALUES ('IgnoredResourcesPolicy', 'Requests satisfying the provided regular expression will be ignored.', NULL, 'Default', 'eye-slash', 'Ignored Resources Policy', NULL, 'class:io.apiman.gateway.engine.policies.IgnoredResourcesPolicy');

INSERT INTO policydefs (id, description, form, form_type, icon, name, plugin_id, policy_impl) VALUES ('URLRewritingPolicy', 'Responses from the back-end API will be modified by fixing up any incorrect URLs found with modified ones.  This is useful because apiman works through an API Gateway.', NULL, 'Default', 'pencil-square', 'URL Rewriting Policy', NULL, 'class:io.apiman.gateway.engine.policies.URLRewritingPolicy');

INSERT INTO policydefs (id, description, form, form_type, icon, name, plugin_id, policy_impl) VALUES ('CachingPolicy', 'Allows caching of API responses in the Gateway to reduce overall traffic to the back-end API.', NULL, 'Default', 'hdd-o', 'Caching Policy', NULL, 'class:io.apiman.gateway.engine.policies.CachingPolicy');

INSERT INTO policydefs (id, description, form, form_type, icon, name, plugin_id, policy_impl) VALUES ('AuthorizationPolicy', 'Enables fine grained authorization to API resources based on authenticated user roles.', NULL, 'Default', 'users', 'Authorization Policy', NULL, 'class:io.apiman.gateway.engine.policies.AuthorizationPolicy');

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/050-apiman-manager-api.db.data.changelog.xml::1434686531709-5::apiman
INSERT INTO roles (id, auto_grant, created_by, created_on, description, name) VALUES ('OrganizationOwner', 1, 'admin', '2015-06-18 17:56:57.496', 'Automatically granted to the user who creates an Organization.  Grants all privileges.', 'Organization Owner');

INSERT INTO roles (id, auto_grant, created_by, created_on, description, name) VALUES ('ApplicationDeveloper', NULL, 'admin', '2015-06-18 17:56:57.632', 'Users responsible for creating and managing applications should be granted this role within an Organization.', 'Application Developer');

INSERT INTO roles (id, auto_grant, created_by, created_on, description, name) VALUES ('ServiceDeveloper', NULL, 'admin', '2015-06-18 17:56:57.641', 'Users responsible for creating and managing services should be granted this role within an Organization.', 'Service Developer');

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/050-apiman-manager-api.db.data.changelog.xml::1434686531709-6::apiman
INSERT INTO users (username, email, full_name, joined_on) VALUES ('admin', 'admin@example.org', 'Admin', '2015-06-18 17:56:54.794');

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-23::apiman (generated)
ALTER TABLE endpoint_properties ADD PRIMARY KEY (service_version_id, name);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-24::apiman (generated)
ALTER TABLE svc_gateways ADD PRIMARY KEY (service_version_id, gateway_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-25::apiman (generated)
ALTER TABLE svc_plans ADD PRIMARY KEY (service_version_id, plan_id, version);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-26::apiman (generated)
ALTER TABLE application_versions ADD PRIMARY KEY (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-27::apiman (generated)
ALTER TABLE applications ADD PRIMARY KEY (id, organization_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-28::apiman (generated)
ALTER TABLE auditlog ADD PRIMARY KEY (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-29::apiman (generated)
ALTER TABLE contracts ADD PRIMARY KEY (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-30::apiman (generated)
ALTER TABLE gateways ADD PRIMARY KEY (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-31::apiman (generated)
ALTER TABLE memberships ADD PRIMARY KEY (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-32::apiman (generated)
ALTER TABLE organizations ADD PRIMARY KEY (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-33::apiman (generated)
ALTER TABLE plan_versions ADD PRIMARY KEY (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-34::apiman (generated)
ALTER TABLE plans ADD PRIMARY KEY (id, organization_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-35::apiman (generated)
ALTER TABLE plugins ADD PRIMARY KEY (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-36::apiman (generated)
ALTER TABLE policies ADD PRIMARY KEY (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-37::apiman (generated)
ALTER TABLE policydefs ADD PRIMARY KEY (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-38::apiman (generated)
ALTER TABLE roles ADD PRIMARY KEY (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-39::apiman (generated)
ALTER TABLE service_defs ADD PRIMARY KEY (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-40::apiman (generated)
ALTER TABLE service_versions ADD PRIMARY KEY (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-41::apiman (generated)
ALTER TABLE services ADD PRIMARY KEY (id, organization_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-42::apiman (generated)
ALTER TABLE users ADD PRIMARY KEY (username);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-43::apiman (generated)
ALTER TABLE services ADD CONSTRAINT FK_31hj3xmhp1wedxjh5bklnlg15 FOREIGN KEY (organization_id) REFERENCES organizations (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-44::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT FK_6h06sgs4dudh1wehmk0us973g FOREIGN KEY (appv_id) REFERENCES application_versions (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-45::apiman (generated)
ALTER TABLE service_defs ADD CONSTRAINT FK_81fuw1n8afmvpw4buk7l4tyxk FOREIGN KEY (service_version_id) REFERENCES service_versions (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-46::apiman (generated)
ALTER TABLE application_versions ADD CONSTRAINT FK_8epnoby31bt7xakegakigpikp FOREIGN KEY (app_id, app_org_id) REFERENCES applications (id, organization_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-47::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT FK_8o6t1f3kg96rxy5uv51f6k9fy FOREIGN KEY (svcv_id) REFERENCES service_versions (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-48::apiman (generated)
ALTER TABLE service_versions ADD CONSTRAINT FK_92erjg9k1lni97gd87nt6tq37 FOREIGN KEY (service_id, service_org_id) REFERENCES services (id, organization_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-49::apiman (generated)
ALTER TABLE endpoint_properties ADD CONSTRAINT FK_gn0ydqur10sxuvpyw2jvv4xxb FOREIGN KEY (service_version_id) REFERENCES service_versions (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-50::apiman (generated)
ALTER TABLE applications ADD CONSTRAINT FK_jenpu34rtuncsgvtw0sfo8qq9 FOREIGN KEY (organization_id) REFERENCES organizations (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-51::apiman (generated)
ALTER TABLE policies ADD CONSTRAINT FK_l4q6we1bos1yl9unmogei6aja FOREIGN KEY (definition_id) REFERENCES policydefs (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-52::apiman (generated)
ALTER TABLE plans ADD CONSTRAINT FK_lwhc7xrdbsun1ak2uvfu0prj8 FOREIGN KEY (organization_id) REFERENCES organizations (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-53::apiman (generated)
ALTER TABLE contracts ADD CONSTRAINT FK_nyw8xu6m8cx4rwwbtrxbjneui FOREIGN KEY (planv_id) REFERENCES plan_versions (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-54::apiman (generated)
ALTER TABLE svc_gateways ADD CONSTRAINT FK_p5dm3cngljt6yrsnvc7uc6a75 FOREIGN KEY (service_version_id) REFERENCES service_versions (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-55::apiman (generated)
ALTER TABLE pd_templates ADD CONSTRAINT FK_prbnn7j7m6m3pxt2dsn9gwlw8 FOREIGN KEY (policydef_id) REFERENCES policydefs (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-56::apiman (generated)
ALTER TABLE permissions ADD CONSTRAINT FK_sq51ihfrapwdr98uufenhcocg FOREIGN KEY (role_id) REFERENCES roles (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-57::apiman (generated)
ALTER TABLE svc_plans ADD CONSTRAINT FK_t7uvfcsswopb9kh8wpa86blqr FOREIGN KEY (service_version_id) REFERENCES service_versions (id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/100-apiman-manager-api.db.constraints.changelog.xml::1436469846462-58::apiman (generated)
ALTER TABLE plan_versions ADD CONSTRAINT FK_tonylvm2ypnq3efxqr1g0m9fs FOREIGN KEY (plan_id, plan_org_id) REFERENCES plans (id, organization_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-1::apiman
ALTER TABLE plugins ADD CONSTRAINT UK_plugins_1 UNIQUE (group_id, artifact_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-2::apiman
ALTER TABLE memberships ADD CONSTRAINT UK_memberships_1 UNIQUE (user_id, role_id, org_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-3::apiman
ALTER TABLE plan_versions ADD CONSTRAINT UK_plan_versions_1 UNIQUE (plan_id, plan_org_id, version);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-4::apiman
ALTER TABLE application_versions ADD CONSTRAINT UK_app_versions_1 UNIQUE (app_id, app_org_id, version);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-5::apiman
ALTER TABLE service_versions ADD CONSTRAINT UK_service_versions_1 UNIQUE (service_id, service_org_id, version);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-6::apiman
ALTER TABLE service_defs ADD CONSTRAINT UK_service_defs_1 UNIQUE (service_version_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/110-apiman-manager-api.db.unique.constraints.changelog.xml::addUniqueConstraint-7::apiman
ALTER TABLE contracts ADD CONSTRAINT UK_contracts_1 UNIQUE (appv_id, svcv_id, planv_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-1::apiman
CREATE INDEX IDX_auditlog_1 ON auditlog(who);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-2::apiman
CREATE INDEX IDX_auditlog_2 ON auditlog(organization_id, entity_id, entity_version, entity_type);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-3::apiman
CREATE INDEX IDX_FK_pd_templates_1 ON pd_templates(policydef_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-4::apiman
CREATE INDEX IDX_users_1 ON users(username);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-5::apiman
CREATE INDEX IDX_users_2 ON users(full_name);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-6::apiman
CREATE INDEX IDX_FK_permissions_1 ON permissions(role_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-7::apiman
CREATE INDEX IDX_memberships_1 ON memberships(user_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-8::apiman
CREATE INDEX IDX_organizations_1 ON organizations(name);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-9::apiman
CREATE INDEX IDX_FK_plans_1 ON plans(organization_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-10::apiman
CREATE INDEX IDX_FK_applications_1 ON applications(organization_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-11::apiman
CREATE INDEX IDX_services_1 ON services(name);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-12::apiman
CREATE INDEX IDX_FK_services_1 ON services(organization_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-13::apiman
CREATE INDEX IDX_policies_1 ON policies(organization_id, entity_id, entity_version);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-14::apiman
CREATE INDEX IDX_policies_2 ON policies(order_index);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-15::apiman
CREATE INDEX IDX_FK_policies_1 ON policies(definition_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-16::apiman
CREATE INDEX IDX_FK_contracts_p ON contracts(planv_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-17::apiman
CREATE INDEX IDX_FK_contracts_s ON contracts(svcv_id);

--  Changeset c:/Users/ewittman/git/apiman/apiman/distro/ddl/src/main/liquibase/current/200-apiman-manager-api.db.indexes.changelog.xml::createIndex-18::apiman
CREATE INDEX IDX_FK_contracts_a ON contracts(appv_id);

