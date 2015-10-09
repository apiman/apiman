/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.api.exportimport;

import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationStatus;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.idm.PermissionBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanStatus;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.services.EndpointType;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceDefinitionType;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.exportimport.beans.MetadataBean;
import io.apiman.manager.api.exportimport.json.JsonGlobalStreamWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.joda.time.DateTime;

@SuppressWarnings("nls")
public class WriteTest {

    public static void main(String... args) throws IOException {
        File f = new File("/tmp/jsonout");
        f.createNewFile();
        FileOutputStream os = new FileOutputStream(f);

        UserBean user = new UserBean();
        user.setAdmin(true);
        user.setEmail("fred@flintstone.com");
        user.setFullName("Frederick Flintstone Jr III");
        user.setJoinedOn(new Date());
        user.setUsername("fflintstone");

        RoleBean role = new RoleBean();
        role.setAutoGrant(true);
        role.setCreatedBy("Fred");
        role.setDescription("This is a role");
        role.setId("123");
        role.setName("CREATOR");
        role.setPermissions(null);

        RoleMembershipBean membership = new RoleMembershipBean();
        membership.setCreatedOn(new Date());
        membership.setId(2L);
        membership.setOrganizationId("123456");
        membership.setRoleId("123");
        membership.setUserId("fflintstone");

        PermissionBean permission = new PermissionBean();
        permission.setName(PermissionType.orgAdmin);
        permission.setOrganizationId("123");

        OrganizationBean orgBean = new OrganizationBean();
        orgBean.setCreatedBy("test");
        orgBean.setCreatedOn(new Date());
        orgBean.setDescription("test description");
        orgBean.setId("123456");
        orgBean.setModifiedBy("John");
        orgBean.setModifiedOn(new Date());
        orgBean.setName("My name is Abraham");

        ApplicationBean app = new ApplicationBean();
        app.setCreatedBy("test");
        app.setCreatedOn(new Date());
        app.setDescription("an app bean");
        app.setId("1");
        app.setName("app-bean");
        app.setOrganization(orgBean); //TODO problemo

        ApplicationVersionBean appBean = new ApplicationVersionBean();
        appBean.setApplication(app);
        appBean.setCreatedBy("test");
        appBean.setCreatedOn(new Date());
        appBean.setId(1234L);
        appBean.setModifiedBy("John");
        appBean.setModifiedOn(new Date());
        appBean.setPublishedOn(new Date());
        appBean.setRetiredOn(null);
        appBean.setStatus(ApplicationStatus.Created);
        appBean.setVersion("1234");

        ServiceBean serviceBean = new ServiceBean();
        serviceBean.setCreatedBy("abc");
        serviceBean.setCreatedOn(new Date());
        serviceBean.setDescription("test test 123");
        serviceBean.setId("SOME-ID");
        serviceBean.setName("SOME-NAME");
        serviceBean.setOrganization(orgBean);

        ServiceVersionBean serviceVerBean = new ServiceVersionBean();
        serviceVerBean.setCreatedBy("Test");
        serviceVerBean.setCreatedOn(new Date());
        serviceVerBean.setDefinitionType(ServiceDefinitionType.None);
        serviceVerBean.setEndpoint("http://localhost:8080/services/echo");
        serviceVerBean.setEndpointType(EndpointType.rest);
        serviceVerBean.setGateways(null); // TODO what is this?
        serviceVerBean.setId(1L);
        serviceVerBean.setModifiedBy("john");
        serviceVerBean.setModifiedOn(new Date());
        serviceVerBean.setPlans(null);
        serviceVerBean.setPublicService(true);
        serviceVerBean.setPublishedOn(new Date());
        serviceVerBean.setRetiredOn(null);
        serviceVerBean.setService(serviceBean);

        PlanBean planBean = new PlanBean();
        planBean.setCreatedBy("abc");
        planBean.setDescription("woo woo woo");
        planBean.setId("plan-1");
        planBean.setOrganization(orgBean);

        PlanVersionBean planVerBean = new PlanVersionBean();
        planVerBean.setCreatedBy("abcd");
        planVerBean.setId(4L);
        planVerBean.setPlan(planBean);
        planVerBean.setStatus(PlanStatus.Ready);

        ContractBean contract = new ContractBean();
        contract.setId(4L);
        contract.setApikey("123456");
        contract.setApplication(appBean);
        contract.setPlan(null);
        contract.setService(serviceVerBean);

        MetadataBean meta = new MetadataBean();
        meta.setApimanVersion("9001");
        meta.setExportedOn(new DateTime());

        JsonGlobalStreamWriter streamer = new JsonGlobalStreamWriter(os, new IStorageTest());

        streamer.writeMetadata(meta);

        streamer.startUsers()
        .writeUser(user)
        .endUsers();

        streamer.startRoles()
        .writeRole(role)
        .endRoles();

        streamer.startOrgs()
        .writeOrg(orgBean)

        .startMemberships()
        .writeMembership(membership)
        .endMemberships()

        .startApplicationVersions()
        .writeApplicationVersion(appBean)
        .endApplicationVersions()

        .startServiceVersions()
        .writeServiceVersion(serviceVerBean)
        .endServiceVersions()

        .startPlanVersions()
        .writePlanVersion(planVerBean)
        .endPlanVersions()

        .startContracts()
        .writeContract(contract)
        .endContracts();
        streamer.endOrgs().close();
    }
}
