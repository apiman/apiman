
--
-- Name: hibernate_sequence
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: auditlog
--

CREATE TABLE auditlog (
    id bigint NOT NULL,
    createdOn timestamp without time zone NOT NULL,
    data text,
    entityId character varying(255),
    entityType character varying(255) NOT NULL,
    entityVersion character varying(255),
    organizationId character varying(255) NOT NULL,
    what character varying(255) NOT NULL,
    who character varying(255) NOT NULL
);

ALTER TABLE ONLY auditlog
    ADD CONSTRAINT PK_auditlog PRIMARY KEY (id);

CREATE INDEX IDX_auditlog_1 ON auditlog (who);
CREATE INDEX IDX_auditlog_2 ON auditlog (organizationId, entityId, entityVersion, entityType);


--
-- Name: plugins
--

CREATE TABLE plugins (
    id character varying(255) NOT NULL,
    groupId character varying(255) NOT NULL,
    artifactId character varying(255) NOT NULL,
    version character varying(255) NOT NULL,
    classifier character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    description character varying(512),
    createdBy character varying(255) NOT NULL,
    createdOn timestamp without time zone NOT NULL,
);

ALTER TABLE ONLY plugins
    ADD CONSTRAINT UK_plugins_1 UNIQUE (groupId, artifactId);

ALTER TABLE ONLY plugins
    ADD CONSTRAINT PK_plugins PRIMARY KEY (id);

--
-- Name: gateways
--

CREATE TABLE gateways (
    id character varying(255) NOT NULL,
    configuration text NOT NULL,
    createdBy character varying(255) NOT NULL,
    createdOn timestamp without time zone NOT NULL,
    description character varying(512),
    modifiedBy character varying(255) NOT NULL,
    modifiedOn timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    type character varying(255) NOT NULL
);

ALTER TABLE ONLY gateways
    ADD CONSTRAINT PK_gateways PRIMARY KEY (id);

--
-- Name: policydefs
--

CREATE TABLE policydefs (
    id character varying(255) NOT NULL,
    description character varying(512) NOT NULL,
    icon character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    policyimpl character varying(255) NOT NULL,
    policyId character varying(255)
);

ALTER TABLE ONLY policydefs
    ADD CONSTRAINT PK_policydefs PRIMARY KEY (id);

ALTER TABLE ONLY policydefs
    ADD CONSTRAINT FK_policydefs_1 FOREIGN KEY (policyId) REFERENCES plugins(id);

CREATE INDEX IDX_FK_policydefs_1 ON policydefs (policyId);

--
-- Name: pd_templates
--

CREATE TABLE pd_templates (
    policydef_id character varying(255) NOT NULL,
    language character varying(255),
    template character varying(2048)
);

ALTER TABLE ONLY pd_templates
    ADD CONSTRAINT FK_pd_templates_1 FOREIGN KEY (policydef_id) REFERENCES policydefs(id);

CREATE INDEX IDX_FK_pd_templates_1 ON pd_templates (policydef_id);

--
-- Name: users
--

CREATE TABLE users (
    username character varying(255) NOT NULL,
    email character varying(255),
    fullName character varying(255),
    joinedOn timestamp without time zone
);

ALTER TABLE ONLY users
    ADD CONSTRAINT PK_users PRIMARY KEY (username);

CREATE INDEX IDX_users_1 ON users (username);
CREATE INDEX IDX_users_2 ON users (fullName);

--
-- Name: roles; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE roles (
    id character varying(255) NOT NULL,
    autogrant boolean,
    createdBy character varying(255) NOT NULL,
    createdOn timestamp without time zone NOT NULL,
    description character varying(512),
    name character varying(255)
);

ALTER TABLE ONLY roles
    ADD CONSTRAINT PK_roles PRIMARY KEY (id);

--
-- Name: permissions
--

CREATE TABLE permissions (
    role_id character varying(255) NOT NULL,
    permissions integer
);

ALTER TABLE ONLY permissions
    ADD CONSTRAINT FK_permissions_1 FOREIGN KEY (role_id) REFERENCES roles(id);

CREATE INDEX IDX_FK_permissions_1 ON permissions (role_id);

--
-- Name: memberships
--

CREATE TABLE memberships (
    id bigint NOT NULL,
    createdOn timestamp without time zone,
    org_id character varying(255),
    role_id character varying(255),
    user_id character varying(255)
);

ALTER TABLE ONLY memberships
    ADD CONSTRAINT PK_memberships PRIMARY KEY (id);

ALTER TABLE ONLY memberships
    ADD CONSTRAINT UK_memberships_1 UNIQUE (user_id, role_id, org_id);

CREATE INDEX IDX_memberships_1 ON memberships (user_id);

--
-- Name: organizations
--

CREATE TABLE organizations (
    id character varying(255) NOT NULL,
    createdBy character varying(255) NOT NULL,
    createdOn timestamp without time zone NOT NULL,
    description character varying(512),
    modifiedBy character varying(255) NOT NULL,
    modifiedOn timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL
);

ALTER TABLE ONLY organizations
    ADD CONSTRAINT PK_organisations PRIMARY KEY (id);

CREATE INDEX IDX_organizations_1 ON organizations (name);

--
-- Name: plans
--

CREATE TABLE plans (
    id character varying(255) NOT NULL,
    createdBy character varying(255) NOT NULL,
    createdOn timestamp without time zone NOT NULL,
    description character varying(512),
    name character varying(255) NOT NULL,
    organizationId character varying(255) NOT NULL
);

ALTER TABLE ONLY plans
    ADD CONSTRAINT PK_plans PRIMARY KEY (id, organizationId);

ALTER TABLE ONLY plans
    ADD CONSTRAINT FK_plans_1 FOREIGN KEY (organizationId) REFERENCES organizations(id);

CREATE INDEX IDX_FK_plans_1 ON plans (organizationId);

--
-- Name: plan_versions
--

CREATE TABLE plan_versions (
    id bigint NOT NULL,
    createdBy character varying(255) NOT NULL,
    createdOn timestamp without time zone NOT NULL,
    lockedOn timestamp without time zone,
    modifiedBy character varying(255) NOT NULL,
    modifiedOn timestamp without time zone NOT NULL,
    status character varying(255) NOT NULL,
    version character varying(255) NOT NULL,
    plan_id character varying(255),
    plan_orgId character varying(255)
);

ALTER TABLE ONLY plan_versions
    ADD CONSTRAINT PK_plan_versions PRIMARY KEY (id);

ALTER TABLE ONLY plan_versions
    ADD CONSTRAINT UK_plan_versions_1 UNIQUE (plan_id, plan_orgId, version);

ALTER TABLE ONLY plan_versions
    ADD CONSTRAINT FK_plan_versions_1 FOREIGN KEY (plan_id, plan_orgId) REFERENCES plans(id, organizationId);

--
-- Name: applications
--

CREATE TABLE applications (
    id character varying(255) NOT NULL,
    createdBy character varying(255) NOT NULL,
    createdOn timestamp without time zone NOT NULL,
    description character varying(512),
    name character varying(255) NOT NULL,
    organizationId character varying(255) NOT NULL
);

ALTER TABLE ONLY applications
    ADD CONSTRAINT PK_applications PRIMARY KEY (id, organizationId);

ALTER TABLE ONLY applications
    ADD CONSTRAINT FK_applications_1 FOREIGN KEY (organizationId) REFERENCES organizations(id);

CREATE INDEX IDX_FK_applications_1 ON applications (organizationId);

--
-- Name: application_versions 
--

CREATE TABLE application_versions (
    id bigint NOT NULL,
    createdBy character varying(255) NOT NULL,
    createdon timestamp without time zone NOT NULL,
    modifiedBy character varying(255) NOT NULL,
    modifiedOn timestamp without time zone NOT NULL,
    publishedOn timestamp without time zone,
    retiredOn timestamp without time zone,
    status character varying(255) NOT NULL,
    version character varying(255) NOT NULL,
    app_id character varying(255),
    app_orgId character varying(255)
);

ALTER TABLE ONLY application_versions
    ADD CONSTRAINT PK_app_versions PRIMARY KEY (id);

ALTER TABLE ONLY application_versions
    ADD CONSTRAINT UK_app_versions_1 UNIQUE (app_id, app_orgId, version);

ALTER TABLE ONLY application_versions
    ADD CONSTRAINT FK_app_versions_1 FOREIGN KEY (app_id, app_orgId) REFERENCES applications(id, organizationId);

--
-- Name: services
--

CREATE TABLE services (
    id character varying(255) NOT NULL,
    createdBy character varying(255) NOT NULL,
    createdOn timestamp without time zone NOT NULL,
    description character varying(512),
    name character varying(255) NOT NULL,
    organizationId character varying(255) NOT NULL
);

ALTER TABLE ONLY services
    ADD CONSTRAINT PK_services PRIMARY KEY (id, organizationId);

ALTER TABLE ONLY services
    ADD CONSTRAINT FK_services_1 FOREIGN KEY (organizationId) REFERENCES organizations(id);

CREATE INDEX IDX_services_1 ON services (name);

CREATE INDEX IDX_FK_services_1 ON services (organizationId);

--
-- Name: service_versions
--

CREATE TABLE service_versions (
    id bigint NOT NULL,
    createdBy character varying(255) NOT NULL,
    createdOn timestamp without time zone NOT NULL,
    endpoint character varying(255),
    endpointType character varying(255),
    modifiedBy character varying(255) NOT NULL,
    modifiedOn timestamp without time zone NOT NULL,
    publicService boolean NOT NULL,
    publishedOn timestamp without time zone,
    retiredOn timestamp without time zone,
    status character varying(255) NOT NULL,
    version character varying(255),
    definitionType character varying(255),
    service_id character varying(255),
    service_orgId character varying(255)
);

ALTER TABLE ONLY service_versions
    ADD CONSTRAINT PK_service_versions PRIMARY KEY (id);

ALTER TABLE ONLY service_versions
    ADD CONSTRAINT UK_service_versions_1 UNIQUE (service_id, service_orgId, version);

ALTER TABLE ONLY service_versions
    ADD CONSTRAINT FK_service_versions_1 FOREIGN KEY (service_id, service_orgId) REFERENCES services(id, organizationId);


--
-- Name: service_defs
--

CREATE TABLE service_defs (
    id bigint NOT NULL,
    serviceVersionId bigint NOT NULL,
    data bytea NOT NULL
);

ALTER TABLE ONLY service_defs
    ADD CONSTRAINT PK_service_defs PRIMARY KEY (id);

ALTER TABLE ONLY service_defs
    ADD CONSTRAINT UK_service_defs_1 UNIQUE (serviceVersionId);

ALTER TABLE ONLY service_defs
    ADD CONSTRAINT FK_service_defs_1 FOREIGN KEY (serviceVersionId) REFERENCES service_versions(id);

--
-- Name: svc_gateways
--

CREATE TABLE svc_gateways (
    service_version_id bigint NOT NULL,
    gatewayId character varying(255) NOT NULL
);

ALTER TABLE ONLY svc_gateways
    ADD CONSTRAINT PK_svc_gateways PRIMARY KEY (service_version_id, gatewayId);

ALTER TABLE ONLY svc_gateways
    ADD CONSTRAINT FK_svc_gateways_1 FOREIGN KEY (service_version_id) REFERENCES service_versions(id);

--
-- Name: svc_plans
--

CREATE TABLE svc_plans (
    service_version_id bigint NOT NULL,
    planId character varying(255) NOT NULL,
    version character varying(255) NOT NULL
);

ALTER TABLE ONLY svc_plans
    ADD CONSTRAINT PK_svc_plans PRIMARY KEY (service_version_id, planId, version);

ALTER TABLE ONLY svc_plans
    ADD CONSTRAINT FK_svc_plans_1 FOREIGN KEY (service_version_id) REFERENCES service_versions(id);

--
-- Name: policies 
--

CREATE TABLE policies (
    id bigint NOT NULL,
    configuration text,
    createdBy character varying(255) NOT NULL,
    createdOn timestamp without time zone NOT NULL,
    entityId character varying(255) NOT NULL,
    entityVersion character varying(255) NOT NULL,
    modifiedBy character varying(255) NOT NULL,
    modifiedOn timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    orderIndex integer NOT NULL,
    organizationId character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    definition_id character varying(255) NOT NULL
);

ALTER TABLE ONLY policies
    ADD CONSTRAINT PK_policies PRIMARY KEY (id);

ALTER TABLE ONLY policies
    ADD CONSTRAINT FK_policies_1 FOREIGN KEY (definition_id) REFERENCES policydefs(id);

CREATE INDEX IDX_policies_1 ON policies (organizationId, entityId, entityVersion);
CREATE INDEX IDX_policies_2 ON policies (orderIndex);
CREATE INDEX IDX_FK_policies_1 ON policies (definition_id);

--
-- Name: contracts
--

CREATE TABLE contracts (
    id bigint NOT NULL,
    apikey character varying(255) NOT NULL,
    createdBy character varying(255) NOT NULL,
    createdOn timestamp without time zone NOT NULL,
    appv_id bigint,
    planv_id bigint,
    svcv_id bigint
);

ALTER TABLE ONLY contracts
    ADD CONSTRAINT PK_contracts PRIMARY KEY (id);

ALTER TABLE ONLY contracts
    ADD CONSTRAINT UK_contracts_1 UNIQUE (appv_id, svcv_id, planv_id);

ALTER TABLE ONLY contracts
    ADD CONSTRAINT FK_contracts_a FOREIGN KEY (appv_id) REFERENCES application_versions(id);

ALTER TABLE ONLY contracts
    ADD CONSTRAINT FK_contracts_s FOREIGN KEY (svcv_id) REFERENCES service_versions(id);

ALTER TABLE ONLY contracts
    ADD CONSTRAINT FK_contracts_p FOREIGN KEY (planv_id) REFERENCES plan_versions(id);

CREATE INDEX IDX_FK_contracts_p ON contracts (planv_id);
CREATE INDEX IDX_FK_contracts_s ON contracts (svcv_id);
CREATE INDEX IDX_FK_contracts_a ON contracts (appv_id);
