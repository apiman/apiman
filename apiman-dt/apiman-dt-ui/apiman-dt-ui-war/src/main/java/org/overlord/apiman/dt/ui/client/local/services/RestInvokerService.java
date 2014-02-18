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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.overlord.apiman.dt.api.beans.idm.UserBean;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.summary.OrganizationSummaryBean;
import org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource;
import org.overlord.apiman.dt.api.rest.contract.IOrganizationResource;
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
    private Caller<IUserResource> user;
    @Inject
    private Caller<IOrganizationResource> organizations;
    
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
     * Gets info about the given user.
     * @param callback
     */
    public void getUser(String userId, IRestInvokerCallback<UserBean> callback) {
        CallbackAdapter<UserBean> adapter = new CallbackAdapter<UserBean>(callback);
        user.call(adapter, adapter).get(userId);
    }

    /**
     * Gets the organizations visible to the current user.
     * @param callback
     */
    public void getUserOrgs(String userId, IRestInvokerCallback<List<OrganizationSummaryBean>> callback) {
        CallbackAdapter<List<OrganizationSummaryBean>> adapter = new CallbackAdapter<List<OrganizationSummaryBean>>(callback);
        user.call(adapter, adapter).getOrganizations(userId);
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

}
