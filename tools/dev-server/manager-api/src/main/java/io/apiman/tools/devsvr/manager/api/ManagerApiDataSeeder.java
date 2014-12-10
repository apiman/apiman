/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.tools.devsvr.manager.api;

import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationStatus;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.gateways.GatewayType;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanStatus;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionTemplateBean;
import io.apiman.manager.api.beans.services.EndpointType;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceGatewayBean;
import io.apiman.manager.api.beans.services.ServicePlanBean;
import io.apiman.manager.api.beans.services.ServiceStatus;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.core.IIdmStorage;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.test.server.DefaultTestDataSeeder;

import java.util.Date;
import java.util.HashSet;

/**
 * Data seeder used for the dtgov dt api dev server.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class ManagerApiDataSeeder extends DefaultTestDataSeeder {
    
    /**
     * Constructor.
     */
    public ManagerApiDataSeeder() {
    }
    
    /**
     * @see io.apiman.manager.test.server.DefaultTestDataSeeder#seed(io.apiman.manager.api.core.IIdmStorage, io.apiman.manager.api.core.IStorage)
     */
    @Override
    public void seed(IIdmStorage idmStorage, IStorage storage) throws StorageException {
        super.seed(idmStorage, storage);
        
        GatewayBean gateway = new GatewayBean();
        gateway.setId("TheGateway");
        gateway.setName("The Gateway");
        gateway.setDescription("The only gateway needed for testing.");
        gateway.setHttpEndpoint("http://localhost:6666/gateway/");
        gateway.setConfiguration("{ \"endpoint\" : \"http://localhost:6666/api/\", \"username\" : \"admin\", \"password\" : \"admin\" }");
        gateway.setType(GatewayType.REST);
        gateway.setCreatedBy("admin");
        gateway.setCreatedOn(new Date());
        gateway.setModifiedBy("admin");
        gateway.setModifiedOn(new Date());
        storage.beginTx();
        storage.createGateway(gateway);
        storage.commitTx();
        
        // Create Organization Owner role
        RoleBean role = new RoleBean();
        role.setId("OrganizationOwner");
        role.setName("Organization Owner");
        role.setAutoGrant(true);
        role.setDescription("This role is automatically given to users when they create an organization.  It grants all permissions.");
        role.setCreatedBy("admin");
        role.setCreatedOn(new Date());
        role.setPermissions(new HashSet<PermissionType>());
        role.getPermissions().add(PermissionType.orgView);
        role.getPermissions().add(PermissionType.orgEdit);
        role.getPermissions().add(PermissionType.orgAdmin);
        role.getPermissions().add(PermissionType.appView);
        role.getPermissions().add(PermissionType.appEdit);
        role.getPermissions().add(PermissionType.appAdmin);
        role.getPermissions().add(PermissionType.planView);
        role.getPermissions().add(PermissionType.planEdit);
        role.getPermissions().add(PermissionType.planAdmin);
        role.getPermissions().add(PermissionType.svcView);
        role.getPermissions().add(PermissionType.svcEdit);
        role.getPermissions().add(PermissionType.svcAdmin);
        idmStorage.createRole(role);

        // Create Application Developer role
        role = new RoleBean();
        role.setId("ApplicationDeveloper");
        role.setName("Application Developer");
        role.setDescription("This role allows users to perform standard application development tasks (manage applications but not services or plans).");
        role.setCreatedBy("admin");
        role.setCreatedOn(new Date());
        role.setPermissions(new HashSet<PermissionType>());
        role.getPermissions().add(PermissionType.orgView);
        role.getPermissions().add(PermissionType.appView);
        role.getPermissions().add(PermissionType.appEdit);
        role.getPermissions().add(PermissionType.appAdmin);
        idmStorage.createRole(role);

        // Create Service Developer role
        role = new RoleBean();
        role.setId("ServiceDeveloper");
        role.setName("Service Developer");
        role.setDescription("This role allows users to perform standard service development tasks such as managing services and plans.");
        role.setCreatedBy("admin");
        role.setCreatedOn(new Date());
        role.setPermissions(new HashSet<PermissionType>());
        role.getPermissions().add(PermissionType.orgView);
        role.getPermissions().add(PermissionType.svcView);
        role.getPermissions().add(PermissionType.svcEdit);
        role.getPermissions().add(PermissionType.svcAdmin);
        role.getPermissions().add(PermissionType.planView);
        role.getPermissions().add(PermissionType.planEdit);
        role.getPermissions().add(PermissionType.planAdmin);
        idmStorage.createRole(role);
        
        storage.beginTx();
        
        // Create JBoss Overlord org
        OrganizationBean org = new OrganizationBean();
        org.setId("JBossOverlord");
        org.setName("JBoss Overlord");
        org.setDescription("Overlord is the umbrella project that will bring governance to the JBoss SOA Platform and eventually beyond.");
        org.setCreatedOn(new Date());
        org.setCreatedBy("admin");
        org.setModifiedOn(new Date());
        org.setModifiedBy("admin");
        storage.createOrganization(org);
        OrganizationBean jbossOrg = org;
        
        // Create Apereo Bedework org
        org = new OrganizationBean();
        org.setId("ApereoBedework");
        org.setName("Apereo Bedework");
        org.setDescription("Bedework is an open-source enterprise calendar system that supports public, personal, and group calendaring.");
        org.setCreatedOn(new Date());
        org.setCreatedBy("admin");
        org.setModifiedOn(new Date());
        org.setModifiedBy("admin");
        storage.createOrganization(org);
        
        storage.commitTx();
        
        // Make admin the owner of both orgs
        RoleMembershipBean membership = RoleMembershipBean.create("admin", "OrganizationOwner", "JBossOverlord");
        membership.setCreatedOn(new Date());
        idmStorage.createMembership(membership);

        membership = RoleMembershipBean.create("admin", "OrganizationOwner", "ApereoBedework");
        membership.setCreatedOn(new Date());
        idmStorage.createMembership(membership);

        storage.beginTx();
        
        // Create some plans
        PlanBean plan = new PlanBean();
        plan.setId("Platinum");
        plan.setName("Platinum");
        plan.setDescription("Provides subscribing applications with full access to the Services in this Organization.");
        plan.setOrganization(jbossOrg);
        plan.setCreatedBy("admin");
        plan.setCreatedOn(new Date());
        storage.createPlan(plan);
        PlanVersionBean pvb = new PlanVersionBean();
        pvb.setVersion("1.0");
        pvb.setStatus(PlanStatus.Created);
        pvb.setPlan(plan);
        pvb.setCreatedBy("admin");
        pvb.setCreatedOn(new Date());
        pvb.setModifiedBy("admin");
        pvb.setModifiedOn(new Date());
        storage.createPlanVersion(pvb);

        plan = new PlanBean();
        plan.setId("Gold");
        plan.setName("Gold");
        plan.setDescription("Provides subscribing applications with full access to a subset of Services. Also allows partial (rate limited) access to the rest.");
        plan.setOrganization(jbossOrg);
        plan.setCreatedBy("admin");
        plan.setCreatedOn(new Date());
        storage.createPlan(plan);
        pvb = new PlanVersionBean();
        pvb.setVersion("1.0");
        pvb.setStatus(PlanStatus.Created);
        pvb.setPlan(plan);
        pvb.setCreatedBy("admin");
        pvb.setCreatedOn(new Date());
        pvb.setModifiedBy("admin");
        pvb.setModifiedOn(new Date());
        storage.createPlanVersion(pvb);
        pvb = new PlanVersionBean();
        pvb.setVersion("1.2");
        pvb.setStatus(PlanStatus.Created);
        pvb.setPlan(plan);
        pvb.setCreatedBy("bwayne");
        pvb.setCreatedOn(new Date());
        pvb.setModifiedBy("bwayne");
        pvb.setModifiedOn(new Date());
        storage.createPlanVersion(pvb);

        // Create some applications
        ApplicationBean app = new ApplicationBean();
        app.setId("dtgov");
        app.setName("dtgov");
        app.setDescription("This is the official Git repository for the Governance DTGov project, which is intended to be a part of the JBoss Overlord.");
        app.setOrganization(jbossOrg);
        app.setCreatedBy("admin");
        app.setCreatedOn(new Date());
        storage.createApplication(app);
        ApplicationVersionBean avb = new ApplicationVersionBean();
        avb.setVersion("1.0");
        avb.setStatus(ApplicationStatus.Created);
        avb.setApplication(app);
        avb.setCreatedBy("admin");
        avb.setCreatedOn(new Date());
        avb.setModifiedBy("admin");
        avb.setModifiedOn(new Date());
        storage.createApplicationVersion(avb);

        app = new ApplicationBean();
        app.setId("rtgov");
        app.setName("rtgov");
        app.setDescription("This component provides the infrastructure to capture service activity information and then correlate...");
        app.setOrganization(jbossOrg);
        app.setCreatedBy("admin");
        app.setCreatedOn(new Date());
        storage.createApplication(app);
        avb = new ApplicationVersionBean();
        avb.setVersion("1.0");
        avb.setStatus(ApplicationStatus.Created);
        avb.setApplication(app);
        avb.setCreatedBy("admin");
        avb.setCreatedOn(new Date());
        avb.setModifiedBy("admin");
        avb.setModifiedOn(new Date());
        storage.createApplicationVersion(avb);

        app = new ApplicationBean();
        app.setId("gadget-server");
        app.setName("gadget-server");
        app.setDescription("This is a project that builds on the Apache Shindig as the open social gadget containers.");
        app.setOrganization(jbossOrg);
        app.setCreatedBy("admin");
        app.setCreatedOn(new Date());
        storage.createApplication(app);
        avb = new ApplicationVersionBean();
        avb.setVersion("1.0");
        avb.setStatus(ApplicationStatus.Created);
        avb.setApplication(app);
        avb.setCreatedBy("admin");
        avb.setCreatedOn(new Date());
        avb.setModifiedBy("admin");
        avb.setModifiedOn(new Date());
        storage.createApplicationVersion(avb);
        
        // Create some services
        ServiceBean service = new ServiceBean();
        service.setId("s-ramp-api");
        service.setName("s-ramp-api");
        service.setDescription("Allows S-RAMP repository users to communicate with the repository via an Atom based API.");
        service.setOrganization(jbossOrg);
        service.setCreatedOn(new Date());
        service.setCreatedBy("admin");
        storage.createService(service);
        ServiceVersionBean svb = new ServiceVersionBean();
        svb.setGateways(new HashSet<ServiceGatewayBean>());
        svb.setPlans(new HashSet<ServicePlanBean>());
        svb.setVersion("1.0");
        svb.setStatus(ServiceStatus.Ready);
        svb.setService(service);
        svb.setCreatedBy("admin");
        svb.setCreatedOn(new Date());
        svb.setModifiedBy("admin");
        svb.setModifiedOn(new Date());
        svb.setEndpoint("http://localhost:9001/echo/s-ramp-server/");
        svb.setEndpointType(EndpointType.rest);
        svb.setGateways(new HashSet<ServiceGatewayBean>());
        ServiceGatewayBean sgb = new ServiceGatewayBean();
        sgb.setGatewayId("TheGateway");
        svb.getGateways().add(sgb);
        ServicePlanBean spb = new ServicePlanBean();
        spb.setPlanId("Gold");
        spb.setVersion("1.0");
        svb.addPlan(spb);
        storage.createServiceVersion(svb);
        
        // Create some policy definitions
        PolicyDefinitionBean whitelistPolicyDef = new PolicyDefinitionBean();
        whitelistPolicyDef.setId("IPWhitelistPolicy");
        whitelistPolicyDef.setName("IP Whitelist Policy");
        whitelistPolicyDef.setDescription("Only requests that originate from a specified set of valid IP addresses will be allowed through.");
        whitelistPolicyDef.setIcon("filter");
        whitelistPolicyDef.setPolicyImpl("class:io.apiman.gateway.engine.policies.IPWhitelistPolicy");
        PolicyDefinitionTemplateBean templateBean = new PolicyDefinitionTemplateBean();
        templateBean.setLanguage(null);
        templateBean.setTemplate("Only requests that originate from the set of @{ipList.size()} configured IP address(es) will be allowed to invoke the managed service.");
        whitelistPolicyDef.getTemplates().add(templateBean);
        storage.createPolicyDefinition(whitelistPolicyDef);

        PolicyDefinitionBean blacklistPolicyDef = new PolicyDefinitionBean();
        blacklistPolicyDef.setId("IPBlacklistPolicy");
        blacklistPolicyDef.setName("IP Blacklist Policy");
        blacklistPolicyDef.setDescription("Only requests that originate from a specified set of valid IP addresses will be allowed through.");
        blacklistPolicyDef.setIcon("thumbs-down");
        blacklistPolicyDef.setPolicyImpl("class:io.apiman.gateway.engine.policies.IPBlacklistPolicy");
        templateBean = new PolicyDefinitionTemplateBean();
        templateBean.setLanguage(null);
        templateBean.setTemplate("Requests that originate from the set of @{ipList.size()} configured IP address(es) will be denied access to the managed service.");
        blacklistPolicyDef.getTemplates().add(templateBean);
        storage.createPolicyDefinition(blacklistPolicyDef);

        PolicyDefinitionBean basicAuthPolicyDef = new PolicyDefinitionBean();
        basicAuthPolicyDef.setId("BASICAuthenticationPolicy");
        basicAuthPolicyDef.setName("BASIC Authentication Policy");
        basicAuthPolicyDef.setDescription("Enables HTTP BASIC Authentication on a service.  Some configuration required.");
        basicAuthPolicyDef.setIcon("lock");
        basicAuthPolicyDef.setPolicyImpl("class:io.apiman.gateway.engine.policies.BasicAuthenticationPolicy");
        templateBean = new PolicyDefinitionTemplateBean();
        templateBean.setLanguage(null);
        templateBean.setTemplate("Access to the service is protected by BASIC Authentication through the '@{realm}' authentication realm.  @if{forwardIdentityHttpHeader != null}Successfully authenticated requests will forward the authenticated identity to the back end service via the '@{forwardIdentityHttpHeader}' custom HTTP header.@end{}");
        basicAuthPolicyDef.getTemplates().add(templateBean);
        storage.createPolicyDefinition(basicAuthPolicyDef);

        PolicyDefinitionBean rateLimitPolicyDef = new PolicyDefinitionBean();
        rateLimitPolicyDef.setId("RateLimitingPolicy");
        rateLimitPolicyDef.setName("Rate Limiting Policy");
        rateLimitPolicyDef.setDescription("Enforces rate configurable request rate limits on a service.  This ensures that consumers can't overload a service with too many requests.");
        rateLimitPolicyDef.setIcon("sliders");
        rateLimitPolicyDef.setPolicyImpl("class:io.apiman.gateway.engine.policies.RateLimitingPolicy");
        templateBean = new PolicyDefinitionTemplateBean();
        templateBean.setLanguage(null);
        templateBean.setTemplate("Consumers are limited to @{limit} requests per @{granularity} per @{period}.");
        rateLimitPolicyDef.getTemplates().add(templateBean);
        storage.createPolicyDefinition(rateLimitPolicyDef);
        
        storage.commitTx();
    }

}
