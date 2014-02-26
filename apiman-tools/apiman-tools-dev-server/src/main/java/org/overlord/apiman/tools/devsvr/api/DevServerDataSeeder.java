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
import org.overlord.apiman.dt.api.beans.idm.PermissionType;
import org.overlord.apiman.dt.api.beans.idm.RoleBean;
import org.overlord.apiman.dt.api.beans.idm.RoleMembershipBean;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.persist.IIdmStorage;
import org.overlord.apiman.dt.api.persist.IStorage;
import org.overlord.apiman.dt.api.persist.StorageException;
import org.overlord.apiman.dt.test.server.DefaultTestDataSeeder;

/**
 * Data seeder used for the dtgov dt api dev server.
 *
 * @author eric.wittmann@redhat.com
 */
public class DevServerDataSeeder extends DefaultTestDataSeeder {
    
    /**
     * Constructor.
     */
    public DevServerDataSeeder() {
    }
    
    /**
     * @see org.overlord.apiman.dt.test.server.DefaultTestDataSeeder#seed(org.overlord.apiman.dt.api.persist.IIdmStorage, org.overlord.apiman.dt.api.persist.IStorage)
     */
    @Override
    public void seed(IIdmStorage idmStorage, IStorage storage) throws StorageException {
        super.seed(idmStorage, storage);
        
        // Create Organization Owner role
        RoleBean role = new RoleBean();
        role.setId("OrganizationOwner");
        role.setName("Organization Owner");
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
        
        // Create JBoss Overlord org
        OrganizationBean org = new OrganizationBean();
        org.setId("JBossOverlord");
        org.setName("JBoss Overlord");
        org.setDescription("Overlord is the umbrella project that will bring governance to the JBoss SOA Platform and eventually beyond.");
        org.setCreatedOn(new Date());
        storage.create(org);
        
        // Create JBoss Overlord org
        org = new OrganizationBean();
        org.setId("ApereoBedework");
        org.setName("Apereo Bedework");
        org.setDescription("Bedework is an open-source enterprise calendar system that supports public, personal, and group calendaring.");
        org.setCreatedOn(new Date());
        storage.create(org);
        
        // Make admin the owner of both orgs
        RoleMembershipBean membership = RoleMembershipBean.create("admin", "OrganizationOwner", "JBossOverlord");
        membership.setCreatedOn(new Date());
        idmStorage.createMembership(membership);

        membership = new RoleMembershipBean();
        membership.setOrganizationId("ApereoBedework");
        membership.setRoleId("OrganizationOwner");
        membership.setUserId("admin");
        idmStorage.createMembership(membership);
        
        ApplicationBean app = new ApplicationBean();
        app.setId("dtgov");
        app.setName("dtgov");
        app.setDescription("This is the official Git repository for the Governance DTGov project, which is intended to be a part of the JBoss Overlord.");
        app.setOrganizationId("JBossOverlord");
        app.setCreatedBy("admin");
        app.setCreatedOn(new Date());
        storage.create(app);

        app = new ApplicationBean();
        app.setId("rtgov");
        app.setName("rtgov");
        app.setDescription("This component provides the infrastructure to capture service activity information and then correlate...");
        app.setOrganizationId("JBossOverlord");
        app.setCreatedBy("admin");
        app.setCreatedOn(new Date());
        storage.create(app);
        
        app = new ApplicationBean();
        app.setId("gadget-server");
        app.setName("gadget-server");
        app.setDescription("This is a project that builds on the Apache Shindig as the open social gadget containers.");
        app.setOrganizationId("JBossOverlord");
        app.setCreatedBy("admin");
        app.setCreatedOn(new Date());
        storage.create(app);
    }

}
