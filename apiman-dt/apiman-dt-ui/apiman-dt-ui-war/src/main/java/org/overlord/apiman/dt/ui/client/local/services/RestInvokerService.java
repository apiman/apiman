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
package org.overlord.apiman.dt.ui.client.local.services;

import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.overlord.apiman.dt.api.beans.apps.ApplicationBean;
import org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean;
import org.overlord.apiman.dt.api.beans.idm.GrantRolesBean;
import org.overlord.apiman.dt.api.beans.idm.RoleBean;
import org.overlord.apiman.dt.api.beans.idm.UserBean;
import org.overlord.apiman.dt.api.beans.members.MemberBean;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.beans.services.ServiceBean;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.OrganizationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.api.rest.contract.IApplicationResource;
import org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource;
import org.overlord.apiman.dt.api.rest.contract.IMemberResource;
import org.overlord.apiman.dt.api.rest.contract.IOrganizationResource;
import org.overlord.apiman.dt.api.rest.contract.IRoleResource;
import org.overlord.apiman.dt.api.rest.contract.IServiceResource;
import org.overlord.apiman.dt.api.rest.contract.ISystemResource;
import org.overlord.apiman.dt.api.rest.contract.IUserResource;
import org.overlord.apiman.dt.ui.client.local.services.rest.CallbackAdapter;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;


/**
 * Used to invoke the APIMan DT REST interface.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class RestInvokerService {
    
    @Inject
    private Caller<ISystemResource> system;
    @Inject
    private Caller<ICurrentUserResource> currentUser;
    @Inject
    private Caller<IRoleResource> roles;
    @Inject
    private Caller<IUserResource> users;
    @Inject
    private Caller<IOrganizationResource> organizations;
    @Inject
    private Caller<IApplicationResource> applications;
    @Inject
    private Caller<IServiceResource> services;
    @Inject
    private Caller<IMemberResource> members;
    
    /**
     * Constructor.
     */
    public RestInvokerService() {
    }

    /**
     * Gets the current system status.
     * @param callback
     */
    public void getSystemStatus(IRestInvokerCallback<String> callback) {
        CallbackAdapter<String> adapter = new CallbackAdapter<String>(callback);
        system.call(adapter, adapter).getStatus();
    }

    /**
     * Gets all roles that can be assigned to users.
     * @param callback
     */
    public void getRoles(IRestInvokerCallback<List<RoleBean>> callback) {
        CallbackAdapter<List<RoleBean>> adapter = new CallbackAdapter<List<RoleBean>>(callback);
        roles.call(adapter, adapter).list();
    }

    /**
     * Gets info about the given user.
     * @param callback
     */
    public void getUser(String userId, IRestInvokerCallback<UserBean> callback) {
        CallbackAdapter<UserBean> adapter = new CallbackAdapter<UserBean>(callback);
        users.call(adapter, adapter).get(userId);
    }
    
    /**
     * Finds users using the given search criteria.
     * @param criteria
     * @param callback
     */
    public void findUsers(SearchCriteriaBean criteria, IRestInvokerCallback<SearchResultsBean<UserBean>> callback) {
        CallbackAdapter<SearchResultsBean<UserBean>> adapter = new CallbackAdapter<SearchResultsBean<UserBean>>(callback);
        users.call(adapter, adapter).search(criteria);
    }

    /**
     * Gets the organizations visible to the given user.
     * @param callback
     */
    public void getUserOrgs(String userId, IRestInvokerCallback<List<OrganizationSummaryBean>> callback) {
        CallbackAdapter<List<OrganizationSummaryBean>> adapter = new CallbackAdapter<List<OrganizationSummaryBean>>(callback);
        users.call(adapter, adapter).getOrganizations(userId);
    }

    /**
     * Gets the applications visible to the given user.
     * @param callback
     */
    public void getUserApps(String userId, IRestInvokerCallback<List<ApplicationSummaryBean>> callback) {
        CallbackAdapter<List<ApplicationSummaryBean>> adapter = new CallbackAdapter<List<ApplicationSummaryBean>>(callback);
        users.call(adapter, adapter).getApplications(userId);
    }

    /**
     * Gets the services visible to the given user.
     * @param callback
     */
    public void getUserServices(String userId, IRestInvokerCallback<List<ServiceSummaryBean>> callback) {
        CallbackAdapter<List<ServiceSummaryBean>> adapter = new CallbackAdapter<List<ServiceSummaryBean>>(callback);
        users.call(adapter, adapter).getServices(userId);
    }

    /**
     * Gets info about the current user.
     * @param callback
     */
    public void getCurrentUserInfo(IRestInvokerCallback<UserBean> callback) {
        CallbackAdapter<UserBean> adapter = new CallbackAdapter<UserBean>(callback);
        currentUser.call(adapter, adapter).getInfo();
    }

    /**
     * Gets the organizations visible to the current user.
     * @param callback
     */
    public void getCurrentUserOrgs(IRestInvokerCallback<List<OrganizationSummaryBean>> callback) {
        CallbackAdapter<List<OrganizationSummaryBean>> adapter = new CallbackAdapter<List<OrganizationSummaryBean>>(callback);
        currentUser.call(adapter, adapter).getOrganizations();
    }

    /**
     * Gets all applications visible to the current user.
     * @param callback
     */
    public void getCurrentUserApps(IRestInvokerCallback<List<ApplicationSummaryBean>> callback) {
        CallbackAdapter<List<ApplicationSummaryBean>> adapter = new CallbackAdapter<List<ApplicationSummaryBean>>(callback);
        currentUser.call(adapter, adapter).getApplications();
    }

    /**
     * Gets all services visible to the current user.
     * @param callback
     */
    public void getCurrentUserServices(IRestInvokerCallback<List<ServiceSummaryBean>> callback) {
        CallbackAdapter<List<ServiceSummaryBean>> adapter = new CallbackAdapter<List<ServiceSummaryBean>>(callback);
        currentUser.call(adapter, adapter).getServices();
    }

    /**
     * Gets an organization by ID.
     * @param orgId
     * @param callback
     */
    public void getOrganization(String orgId, IRestInvokerCallback<OrganizationBean> callback) {
        CallbackAdapter<OrganizationBean> adapter = new CallbackAdapter<OrganizationBean>(callback);
        organizations.call(adapter, adapter).get(orgId);
    }

    /**
     * Creates a new organization.
     * @param org
     * @param callback
     */
    public void createOrganization(OrganizationBean org, IRestInvokerCallback<OrganizationBean> callback) {
        CallbackAdapter<OrganizationBean> adapter = new CallbackAdapter<OrganizationBean>(callback);
        organizations.call(adapter, adapter).create(org);
    }

    /**
     * Creates a new application.
     * @param organizationId
     * @param app
     * @param callback
     */
    public void createApplication(String organizationId, ApplicationBean app, IRestInvokerCallback<ApplicationBean> callback) {
        CallbackAdapter<ApplicationBean> adapter = new CallbackAdapter<ApplicationBean>(callback);
        applications.call(adapter, adapter).create(organizationId, app);
    }
    
    /**
     * Creates a new version of an app.
     * @param organizationId
     * @param applicationId
     * @param version
     * @param callback
     */
    public void createApplicationVersion(String organizationId, String applicationId, ApplicationVersionBean version,
            IRestInvokerCallback<ApplicationVersionBean> callback) {
        CallbackAdapter<ApplicationVersionBean> adapter = new CallbackAdapter<ApplicationVersionBean>(callback);
        applications.call(adapter, adapter).createVersion(organizationId, applicationId, version);
    }

    /**
     * Gets an application.
     * @param organizationId
     * @param applicationId
     * @param callback
     */
    public void getApplication(String organizationId, String applicationId, IRestInvokerCallback<ApplicationBean> callback) {
        CallbackAdapter<ApplicationBean> adapter = new CallbackAdapter<ApplicationBean>(callback);
        applications.call(adapter, adapter).get(organizationId, applicationId);
    }

    /**
     * Gets all versions of the application.
     * @param organizationId
     * @param applicationId
     * @param callback
     */
    public void getApplicationVersions(String organizationId, String applicationId, 
            IRestInvokerCallback<List<ApplicationVersionBean>> callback) {
        CallbackAdapter<List<ApplicationVersionBean>> adapter = new CallbackAdapter<List<ApplicationVersionBean>>(callback);
        applications.call(adapter, adapter).listVersions(organizationId, applicationId);
    }

    /**
     * Gets all applications in the organization.
     * @param organizationId
     * @param applicationId
     * @param callback
     */
    public void getApplications(String organizationId, IRestInvokerCallback<List<ApplicationSummaryBean>> callback) {
        CallbackAdapter<List<ApplicationSummaryBean>> adapter = new CallbackAdapter<List<ApplicationSummaryBean>>(callback);
        applications.call(adapter, adapter).list(organizationId);
    }
    
    /**
     * Creates a new service.
     * @param organizationId
     * @param service
     * @param callback
     */
    public void createService(String organizationId, ServiceBean service, IRestInvokerCallback<ServiceBean> callback) {
        CallbackAdapter<ServiceBean> adapter = new CallbackAdapter<ServiceBean>(callback);
        services.call(adapter, adapter).create(organizationId, service);
    }
    
    /**
     * Creates a new version of an service.
     * @param organizationId
     * @param serviceId
     * @param version
     * @param callback
     */
    public void createServiceVersion(String organizationId, String serviceId, ServiceVersionBean version,
            IRestInvokerCallback<ServiceVersionBean> callback) {
        CallbackAdapter<ServiceVersionBean> adapter = new CallbackAdapter<ServiceVersionBean>(callback);
        services.call(adapter, adapter).createVersion(organizationId, serviceId, version);
    }

    /**
     * Gets an service.
     * @param organizationId
     * @param serviceId
     * @param callback
     */
    public void getService(String organizationId, String serviceId, IRestInvokerCallback<ServiceBean> callback) {
        CallbackAdapter<ServiceBean> adapter = new CallbackAdapter<ServiceBean>(callback);
        services.call(adapter, adapter).get(organizationId, serviceId);
    }

    /**
     * Gets all versions of the service.
     * @param organizationId
     * @param serviceId
     * @param callback
     */
    public void getServiceVersions(String organizationId, String serviceId,
            IRestInvokerCallback<List<ServiceVersionBean>> callback) {
        CallbackAdapter<List<ServiceVersionBean>> adapter = new CallbackAdapter<List<ServiceVersionBean>>(callback);
        services.call(adapter, adapter).listVersions(organizationId, serviceId);
    }

    /**
     * Gets all services in the organization.
     * @param organizationId
     * @param serviceId
     * @param callback
     */
    public void getServices(String organizationId, IRestInvokerCallback<List<ServiceSummaryBean>> callback) {
        CallbackAdapter<List<ServiceSummaryBean>> adapter = new CallbackAdapter<List<ServiceSummaryBean>>(callback);
        services.call(adapter, adapter).list(organizationId);
    }

    /**
     * Gets all members of an org.
     * @param organizationId
     * @param callback
     */
    public void getOrgMembers(String organizationId, IRestInvokerCallback<List<MemberBean>> callback) {
        CallbackAdapter<List<MemberBean>> adapter = new CallbackAdapter<List<MemberBean>>(callback);
        members.call(adapter, adapter).listMembers(organizationId);
    }
    
    /**
     * Grants a role to a user.
     * @param organizationId
     * @param userId
     * @param roleIds
     * @param callback
     */
    public void grant(String organizationId, String userId, Set<String> roleIds, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        GrantRolesBean bean = new GrantRolesBean();
        bean.setUserId(userId);
        bean.setRoleIds(roleIds);
        members.call(adapter, adapter).grant(organizationId, bean);
    }

    /**
     * Revokes a role from the user.
     * @param organizationId
     * @param userId
     * @param roleId
     * @param callback
     */
    public void revoke(String organizationId, String userId, String roleId, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        members.call(adapter, adapter).revoke(organizationId, roleId, userId);
    }

    /**
     * Revokes a role from the user.
     * @param organizationId
     * @param userId
     * @param callback
     */
    public void revokeAll(String organizationId, String userId, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        members.call(adapter, adapter).revokeAll(organizationId, userId);
    }


}
