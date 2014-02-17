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
        role.getPermissions().add(PermissionType.orgUpdate.toString());
        role.getPermissions().add(PermissionType.orgDelete.toString());
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
        
        // Make eric the owner of both orgs
        RoleMembershipBean membership = new RoleMembershipBean();
        membership.setOrganizationId("JBossOverlord");
        membership.setRoleId("OrganizationOwner");
        membership.setUserId("eric");
        idmStorage.createMembership(membership);

        membership = new RoleMembershipBean();
        membership.setOrganizationId("ApereoBedework");
        membership.setRoleId("OrganizationOwner");
        membership.setUserId("eric");
        idmStorage.createMembership(membership);
    }

}
