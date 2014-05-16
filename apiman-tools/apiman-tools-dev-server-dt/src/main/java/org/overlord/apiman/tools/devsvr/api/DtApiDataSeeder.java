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
package org.overlord.apiman.tools.devsvr.api;

import java.util.Date;
import java.util.HashSet;

import org.overlord.apiman.dt.api.beans.apps.ApplicationBean;
import org.overlord.apiman.dt.api.beans.apps.ApplicationStatus;
import org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean;
import org.overlord.apiman.dt.api.beans.idm.PermissionType;
import org.overlord.apiman.dt.api.beans.idm.RoleBean;
import org.overlord.apiman.dt.api.beans.idm.RoleMembershipBean;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.plans.PlanBean;
import org.overlord.apiman.dt.api.beans.plans.PlanStatus;
import org.overlord.apiman.dt.api.beans.plans.PlanVersionBean;
import org.overlord.apiman.dt.api.beans.services.EndpointType;
import org.overlord.apiman.dt.api.beans.services.ServiceBean;
import org.overlord.apiman.dt.api.beans.services.ServicePlanBean;
import org.overlord.apiman.dt.api.beans.services.ServiceStatus;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.core.IIdmStorage;
import org.overlord.apiman.dt.api.core.IStorage;
import org.overlord.apiman.dt.api.core.exceptions.StorageException;
import org.overlord.apiman.dt.test.server.DefaultTestDataSeeder;

/**
 * Data seeder used for the dtgov dt api dev server.
 *
 * @author eric.wittmann@redhat.com
 */
public class DtApiDataSeeder extends DefaultTestDataSeeder {
    
    /**
     * Constructor.
     */
    public DtApiDataSeeder() {
    }
    
    /**
     * @see org.overlord.apiman.dt.test.server.DefaultTestDataSeeder#seed(org.overlord.apiman.dt.api.core.IIdmStorage, org.overlord.apiman.dt.api.core.IStorage)
     */
    @Override
    public void seed(IIdmStorage idmStorage, IStorage storage) throws StorageException {
        super.seed(idmStorage, storage);
        
        // Create Organization Owner role
        RoleBean role = new RoleBean();
        role.setId("OrganizationOwner"); //$NON-NLS-1$
        role.setName("Organization Owner"); //$NON-NLS-1$
        role.setPermissions(new HashSet<String>());
        role.getPermissions().add(PermissionType.orgView.toString());
        role.getPermissions().add(PermissionType.orgEdit.toString());
        role.getPermissions().add(PermissionType.orgAdmin.toString());
        role.getPermissions().add(PermissionType.appView.toString());
        role.getPermissions().add(PermissionType.appEdit.toString());
        role.getPermissions().add(PermissionType.appAdmin.toString());
        role.getPermissions().add(PermissionType.planView.toString());
        role.getPermissions().add(PermissionType.planEdit.toString());
        role.getPermissions().add(PermissionType.planAdmin.toString());
        role.getPermissions().add(PermissionType.svcView.toString());
        role.getPermissions().add(PermissionType.svcEdit.toString());
        role.getPermissions().add(PermissionType.svcAdmin.toString());
        idmStorage.createRole(role);

        // Create Application Developer role
        role = new RoleBean();
        role.setId("ApplicationDeveloper"); //$NON-NLS-1$
        role.setName("Application Developer"); //$NON-NLS-1$
        role.setPermissions(new HashSet<String>());
        role.getPermissions().add(PermissionType.orgView.toString());
        role.getPermissions().add(PermissionType.appView.toString());
        role.getPermissions().add(PermissionType.appEdit.toString());
        role.getPermissions().add(PermissionType.appAdmin.toString());
        idmStorage.createRole(role);

        // Create Service Developer role
        role = new RoleBean();
        role.setId("ServiceDeveloper"); //$NON-NLS-1$
        role.setName("Service Developer"); //$NON-NLS-1$
        role.setPermissions(new HashSet<String>());
        role.getPermissions().add(PermissionType.orgView.toString());
        role.getPermissions().add(PermissionType.svcView.toString());
        role.getPermissions().add(PermissionType.svcEdit.toString());
        role.getPermissions().add(PermissionType.svcAdmin.toString());
        role.getPermissions().add(PermissionType.planView.toString());
        role.getPermissions().add(PermissionType.planEdit.toString());
        role.getPermissions().add(PermissionType.planAdmin.toString());
        idmStorage.createRole(role);
        
        // Create JBoss Overlord org
        OrganizationBean org = new OrganizationBean();
        org.setId("JBossOverlord"); //$NON-NLS-1$
        org.setName("JBoss Overlord"); //$NON-NLS-1$
        org.setDescription("Overlord is the umbrella project that will bring governance to the JBoss SOA Platform and eventually beyond."); //$NON-NLS-1$
        org.setCreatedOn(new Date());
        org.setCreatedBy("admin"); //$NON-NLS-1$
        storage.create(org);
        
        // Create Apereo Bedework org
        org = new OrganizationBean();
        org.setId("ApereoBedework"); //$NON-NLS-1$
        org.setName("Apereo Bedework"); //$NON-NLS-1$
        org.setDescription("Bedework is an open-source enterprise calendar system that supports public, personal, and group calendaring."); //$NON-NLS-1$
        org.setCreatedOn(new Date());
        org.setCreatedBy("admin"); //$NON-NLS-1$
        storage.create(org);
        
        // Make admin the owner of both orgs
        RoleMembershipBean membership = RoleMembershipBean.create("admin", "OrganizationOwner", "JBossOverlord"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        membership.setCreatedOn(new Date());
        idmStorage.createMembership(membership);

        membership = RoleMembershipBean.create("admin", "OrganizationOwner", "ApereoBedework"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        membership.setCreatedOn(new Date());
        idmStorage.createMembership(membership);

        // Create some plans
        PlanBean plan = new PlanBean();
        plan.setId("Platinum"); //$NON-NLS-1$
        plan.setName("Platinum"); //$NON-NLS-1$
        plan.setDescription("Provides subscribing applications with full access to the Services in this Organization."); //$NON-NLS-1$
        plan.setOrganizationId("JBossOverlord"); //$NON-NLS-1$
        plan.setCreatedBy("admin"); //$NON-NLS-1$
        plan.setCreatedOn(new Date());
        storage.create(plan);
        PlanVersionBean pvb = new PlanVersionBean();
        pvb.setVersion("1.0"); //$NON-NLS-1$
        pvb.setStatus(PlanStatus.Created);
        pvb.setPlan(plan);
        pvb.setCreatedBy("admin"); //$NON-NLS-1$
        pvb.setCreatedOn(new Date());
        storage.create(pvb);

        plan = new PlanBean();
        plan.setId("Gold"); //$NON-NLS-1$
        plan.setName("Gold"); //$NON-NLS-1$
        plan.setDescription("Provides subscribing applications with full access to a subset of Services. Also allows partial (rate limited) access to the rest."); //$NON-NLS-1$
        plan.setOrganizationId("JBossOverlord"); //$NON-NLS-1$
        plan.setCreatedBy("admin"); //$NON-NLS-1$
        plan.setCreatedOn(new Date());
        storage.create(plan);
        pvb = new PlanVersionBean();
        pvb.setVersion("1.0"); //$NON-NLS-1$
        pvb.setStatus(PlanStatus.Created);
        pvb.setPlan(plan);
        pvb.setCreatedBy("admin"); //$NON-NLS-1$
        pvb.setCreatedOn(new Date());
        storage.create(pvb);
        pvb = new PlanVersionBean();
        pvb.setVersion("1.2"); //$NON-NLS-1$
        pvb.setStatus(PlanStatus.Created);
        pvb.setPlan(plan);
        pvb.setCreatedBy("admin"); //$NON-NLS-1$
        pvb.setCreatedOn(new Date());
        storage.create(pvb);

        // Create some applications
        ApplicationBean app = new ApplicationBean();
        app.setId("dtgov"); //$NON-NLS-1$
        app.setName("dtgov"); //$NON-NLS-1$
        app.setDescription("This is the official Git repository for the Governance DTGov project, which is intended to be a part of the JBoss Overlord."); //$NON-NLS-1$
        app.setOrganizationId("JBossOverlord"); //$NON-NLS-1$
        app.setCreatedBy("admin"); //$NON-NLS-1$
        app.setCreatedOn(new Date());
        storage.create(app);
        ApplicationVersionBean avb = new ApplicationVersionBean();
        avb.setVersion("1.0"); //$NON-NLS-1$
        avb.setStatus(ApplicationStatus.Created);
        avb.setApplication(app);
        avb.setCreatedBy("admin"); //$NON-NLS-1$
        avb.setCreatedOn(new Date());
        storage.create(avb);

        app = new ApplicationBean();
        app.setId("rtgov"); //$NON-NLS-1$
        app.setName("rtgov"); //$NON-NLS-1$
        app.setDescription("This component provides the infrastructure to capture service activity information and then correlate..."); //$NON-NLS-1$
        app.setOrganizationId("JBossOverlord"); //$NON-NLS-1$
        app.setCreatedBy("admin"); //$NON-NLS-1$
        app.setCreatedOn(new Date());
        storage.create(app);
        avb = new ApplicationVersionBean();
        avb.setVersion("1.0"); //$NON-NLS-1$
        avb.setStatus(ApplicationStatus.Created);
        avb.setApplication(app);
        avb.setCreatedBy("admin"); //$NON-NLS-1$
        avb.setCreatedOn(new Date());
        storage.create(avb);

        app = new ApplicationBean();
        app.setId("gadget-server"); //$NON-NLS-1$
        app.setName("gadget-server"); //$NON-NLS-1$
        app.setDescription("This is a project that builds on the Apache Shindig as the open social gadget containers."); //$NON-NLS-1$
        app.setOrganizationId("JBossOverlord"); //$NON-NLS-1$
        app.setCreatedBy("admin"); //$NON-NLS-1$
        app.setCreatedOn(new Date());
        storage.create(app);
        avb = new ApplicationVersionBean();
        avb.setVersion("1.0"); //$NON-NLS-1$
        avb.setStatus(ApplicationStatus.Created);
        avb.setApplication(app);
        avb.setCreatedBy("admin"); //$NON-NLS-1$
        avb.setCreatedOn(new Date());
        storage.create(avb);
        
        // Create some services
        ServiceBean service = new ServiceBean();
        service.setId("s-ramp-api"); //$NON-NLS-1$
        service.setName("s-ramp-api"); //$NON-NLS-1$
        service.setDescription("Allows S-RAMP repository users to communicate with the repository via an Atom based API."); //$NON-NLS-1$
        service.setOrganizationId("JBossOverlord"); //$NON-NLS-1$
        service.setCreatedOn(new Date());
        service.setCreatedBy("admin"); //$NON-NLS-1$
        storage.create(service);
        ServiceVersionBean svb = new ServiceVersionBean();
        svb.setVersion("1.0"); //$NON-NLS-1$
        svb.setStatus(ServiceStatus.Ready);
        svb.setService(service);
        svb.setCreatedBy("admin"); //$NON-NLS-1$
        svb.setCreatedOn(new Date());
        svb.setEndpoint("http://localhost:9001/echo/s-ramp-server/"); //$NON-NLS-1$
        svb.setEndpointType(EndpointType.rest);
        ServicePlanBean spb = new ServicePlanBean();
        spb.setPlanId("Gold"); //$NON-NLS-1$
        spb.setVersion("1.0"); //$NON-NLS-1$
        svb.addPlan(spb);
        storage.create(svb);
    }

}
