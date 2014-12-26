--
-- Name: application_versions; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE application_versions (
    id bigint NOT NULL,
    createdby character varying(255) NOT NULL,
    createdon timestamp without time zone NOT NULL,
    modifiedby character varying(255) NOT NULL,
    modifiedon timestamp without time zone NOT NULL,
    publishedon timestamp without time zone,
    retiredon timestamp without time zone,
    status character varying(255) NOT NULL,
    version character varying(255) NOT NULL,
    app_id character varying(255),
    app_orgid character varying(255)
);


--
-- Name: applications; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE applications (
    id character varying(255) NOT NULL,
    createdby character varying(255) NOT NULL,
    createdon timestamp without time zone NOT NULL,
    description character varying(512),
    name character varying(255) NOT NULL,
    organizationid character varying(255) NOT NULL
);


--
-- Name: auditlog; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE auditlog (
    id bigint NOT NULL,
    createdon timestamp without time zone NOT NULL,
    data text,
    entityid character varying(255),
    entitytype character varying(255) NOT NULL,
    entityversion character varying(255),
    organizationid character varying(255) NOT NULL,
    what character varying(255) NOT NULL,
    who character varying(255) NOT NULL
);


--
-- Name: contracts; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE contracts (
    id bigint NOT NULL,
    apikey character varying(255) NOT NULL,
    createdby character varying(255) NOT NULL,
    createdon timestamp without time zone NOT NULL,
    appv_id bigint,
    planv_id bigint,
    svcv_id bigint
);


--
-- Name: gateways; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE gateways (
    id character varying(255) NOT NULL,
    configuration text NOT NULL,
    createdby character varying(255) NOT NULL,
    createdon timestamp without time zone NOT NULL,
    description character varying(512),
    modifiedby character varying(255) NOT NULL,
    modifiedon timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    type character varying(255) NOT NULL
);


--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: memberships; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE memberships (
    id bigint NOT NULL,
    createdon timestamp without time zone,
    org_id character varying(255),
    role_id character varying(255),
    user_id character varying(255)
);


--
-- Name: organizations; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE organizations (
    id character varying(255) NOT NULL,
    createdby character varying(255) NOT NULL,
    createdon timestamp without time zone NOT NULL,
    description character varying(512),
    modifiedby character varying(255) NOT NULL,
    modifiedon timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL
);


--
-- Name: pd_templates; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE pd_templates (
    policydef_id character varying(255) NOT NULL,
    language character varying(255),
    template character varying(2048)
);


--
-- Name: permissions; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE permissions (
    role_id character varying(255) NOT NULL,
    permissions integer
);


--
-- Name: plan_versions; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE plan_versions (
    id bigint NOT NULL,
    createdby character varying(255) NOT NULL,
    createdon timestamp without time zone NOT NULL,
    lockedon timestamp without time zone,
    modifiedby character varying(255) NOT NULL,
    modifiedon timestamp without time zone NOT NULL,
    status character varying(255) NOT NULL,
    version character varying(255) NOT NULL,
    plan_id character varying(255),
    plan_orgid character varying(255)
);


--
-- Name: plans; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE plans (
    id character varying(255) NOT NULL,
    createdby character varying(255) NOT NULL,
    createdon timestamp without time zone NOT NULL,
    description character varying(512),
    name character varying(255) NOT NULL,
    organizationid character varying(255) NOT NULL
);


--
-- Name: policies; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE policies (
    id bigint NOT NULL,
    configuration text,
    createdby character varying(255) NOT NULL,
    createdon timestamp without time zone NOT NULL,
    entityid character varying(255) NOT NULL,
    entityversion character varying(255) NOT NULL,
    modifiedby character varying(255) NOT NULL,
    modifiedon timestamp without time zone NOT NULL,
    name character varying(255) NOT NULL,
    orderindex integer NOT NULL,
    organizationid character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    definition_id character varying(255) NOT NULL
);


--
-- Name: policydefs; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE policydefs (
    id character varying(255) NOT NULL,
    description character varying(512) NOT NULL,
    icon character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    policyimpl character varying(255) NOT NULL
);


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE roles (
    id character varying(255) NOT NULL,
    autogrant boolean,
    createdby character varying(255) NOT NULL,
    createdon timestamp without time zone NOT NULL,
    description character varying(512),
    name character varying(255)
);


--
-- Name: service_versions; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE service_versions (
    id bigint NOT NULL,
    createdby character varying(255) NOT NULL,
    createdon timestamp without time zone NOT NULL,
    endpoint character varying(255),
    endpointtype character varying(255),
    modifiedby character varying(255) NOT NULL,
    modifiedon timestamp without time zone NOT NULL,
    publicservice boolean NOT NULL,
    publishedon timestamp without time zone,
    retiredon timestamp without time zone,
    status character varying(255) NOT NULL,
    version character varying(255),
    service_id character varying(255),
    service_orgid character varying(255)
);


--
-- Name: services; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE services (
    id character varying(255) NOT NULL,
    createdby character varying(255) NOT NULL,
    createdon timestamp without time zone NOT NULL,
    description character varying(512),
    name character varying(255) NOT NULL,
    organizationid character varying(255) NOT NULL
);


--
-- Name: svc_gateways; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE svc_gateways (
    service_version_id bigint NOT NULL,
    gatewayid character varying(255) NOT NULL
);


--
-- Name: svc_plans; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE svc_plans (
    service_version_id bigint NOT NULL,
    planid character varying(255) NOT NULL,
    version character varying(255) NOT NULL
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE users (
    username character varying(255) NOT NULL,
    email character varying(255),
    fullname character varying(255),
    joinedon timestamp without time zone
);


--
-- Name: application_versions_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY application_versions
    ADD CONSTRAINT application_versions_pkey PRIMARY KEY (id);


--
-- Name: applications_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY applications
    ADD CONSTRAINT applications_pkey PRIMARY KEY (id, organizationid);


--
-- Name: auditlog_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY auditlog
    ADD CONSTRAINT auditlog_pkey PRIMARY KEY (id);


--
-- Name: contracts_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY contracts
    ADD CONSTRAINT contracts_pkey PRIMARY KEY (id);


--
-- Name: gateways_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY gateways
    ADD CONSTRAINT gateways_pkey PRIMARY KEY (id);


--
-- Name: memberships_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY memberships
    ADD CONSTRAINT memberships_pkey PRIMARY KEY (id);


--
-- Name: organizations_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY organizations
    ADD CONSTRAINT organizations_pkey PRIMARY KEY (id);


--
-- Name: plan_versions_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY plan_versions
    ADD CONSTRAINT plan_versions_pkey PRIMARY KEY (id);


--
-- Name: plans_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY plans
    ADD CONSTRAINT plans_pkey PRIMARY KEY (id, organizationid);


--
-- Name: policies_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY policies
    ADD CONSTRAINT policies_pkey PRIMARY KEY (id);


--
-- Name: policydefs_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY policydefs
    ADD CONSTRAINT policydefs_pkey PRIMARY KEY (id);


--
-- Name: roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: service_versions_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY service_versions
    ADD CONSTRAINT service_versions_pkey PRIMARY KEY (id);


--
-- Name: services_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY services
    ADD CONSTRAINT services_pkey PRIMARY KEY (id, organizationid);


--
-- Name: svc_gateways_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY svc_gateways
    ADD CONSTRAINT svc_gateways_pkey PRIMARY KEY (service_version_id, gatewayid);


--
-- Name: svc_plans_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY svc_plans
    ADD CONSTRAINT svc_plans_pkey PRIMARY KEY (service_version_id, planid, version);


--
-- Name: uk_40frt930c3e2thea2xugkcfl1; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY plan_versions
    ADD CONSTRAINT uk_40frt930c3e2thea2xugkcfl1 UNIQUE (plan_id, plan_orgid, version);


--
-- Name: uk_fwy6c61bcnvshcsxubanjvi08; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY memberships
    ADD CONSTRAINT uk_fwy6c61bcnvshcsxubanjvi08 UNIQUE (user_id, role_id, org_id);


--
-- Name: uk_geud1ialqq0rkjkugx8td8roj; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY service_versions
    ADD CONSTRAINT uk_geud1ialqq0rkjkugx8td8roj UNIQUE (service_id, service_orgid, version);


--
-- Name: uk_i3bq88cawo5mi1kf3pk8fkvsm; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY contracts
    ADD CONSTRAINT uk_i3bq88cawo5mi1kf3pk8fkvsm UNIQUE (appv_id, svcv_id, planv_id);


--
-- Name: uk_tl14u5fyq6fbg69trbs1ubq1t; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY application_versions
    ADD CONSTRAINT uk_tl14u5fyq6fbg69trbs1ubq1t UNIQUE (app_id, app_orgid, version);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (username);


--
-- Name: fk_2hn0ec7u04iu5dqythleuykqm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY service_versions
    ADD CONSTRAINT fk_2hn0ec7u04iu5dqythleuykqm FOREIGN KEY (service_id, service_orgid) REFERENCES services(id, organizationid);


--
-- Name: fk_6h06sgs4dudh1wehmk0us973g; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contracts
    ADD CONSTRAINT fk_6h06sgs4dudh1wehmk0us973g FOREIGN KEY (appv_id) REFERENCES application_versions(id);


--
-- Name: fk_8o6t1f3kg96rxy5uv51f6k9fy; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contracts
    ADD CONSTRAINT fk_8o6t1f3kg96rxy5uv51f6k9fy FOREIGN KEY (svcv_id) REFERENCES service_versions(id);


--
-- Name: fk_918ai8n5wkp4k6jr1k5selmfj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY plans
    ADD CONSTRAINT fk_918ai8n5wkp4k6jr1k5selmfj FOREIGN KEY (organizationid) REFERENCES organizations(id);


--
-- Name: fk_bbw8tg544g6n2w8n1w4tu29w4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY services
    ADD CONSTRAINT fk_bbw8tg544g6n2w8n1w4tu29w4 FOREIGN KEY (organizationid) REFERENCES organizations(id);


--
-- Name: fk_i2d88hsgy9siaafrf926b4pp6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY application_versions
    ADD CONSTRAINT fk_i2d88hsgy9siaafrf926b4pp6 FOREIGN KEY (app_id, app_orgid) REFERENCES applications(id, organizationid);


--
-- Name: fk_l4q6we1bos1yl9unmogei6aja; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY policies
    ADD CONSTRAINT fk_l4q6we1bos1yl9unmogei6aja FOREIGN KEY (definition_id) REFERENCES policydefs(id);


--
-- Name: fk_nlj9eihpqnwxn5c2385y6hg8g; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY applications
    ADD CONSTRAINT fk_nlj9eihpqnwxn5c2385y6hg8g FOREIGN KEY (organizationid) REFERENCES organizations(id);


--
-- Name: fk_nyw8xu6m8cx4rwwbtrxbjneui; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY contracts
    ADD CONSTRAINT fk_nyw8xu6m8cx4rwwbtrxbjneui FOREIGN KEY (planv_id) REFERENCES plan_versions(id);


--
-- Name: fk_p5dm3cngljt6yrsnvc7uc6a75; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY svc_gateways
    ADD CONSTRAINT fk_p5dm3cngljt6yrsnvc7uc6a75 FOREIGN KEY (service_version_id) REFERENCES service_versions(id);


--
-- Name: fk_prbnn7j7m6m3pxt2dsn9gwlw8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY pd_templates
    ADD CONSTRAINT fk_prbnn7j7m6m3pxt2dsn9gwlw8 FOREIGN KEY (policydef_id) REFERENCES policydefs(id);


--
-- Name: fk_rjp9ytrw1o5jq2bn034844qmk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY plan_versions
    ADD CONSTRAINT fk_rjp9ytrw1o5jq2bn034844qmk FOREIGN KEY (plan_id, plan_orgid) REFERENCES plans(id, organizationid);


--
-- Name: fk_sq51ihfrapwdr98uufenhcocg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY permissions
    ADD CONSTRAINT fk_sq51ihfrapwdr98uufenhcocg FOREIGN KEY (role_id) REFERENCES roles(id);


--
-- Name: fk_t7uvfcsswopb9kh8wpa86blqr; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY svc_plans
    ADD CONSTRAINT fk_t7uvfcsswopb9kh8wpa86blqr FOREIGN KEY (service_version_id) REFERENCES service_versions(id);
