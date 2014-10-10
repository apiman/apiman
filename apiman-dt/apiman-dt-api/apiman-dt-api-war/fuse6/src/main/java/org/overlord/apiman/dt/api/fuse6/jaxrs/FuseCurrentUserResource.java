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
package org.overlord.apiman.dt.api.fuse6.jaxrs;

import java.util.List;

import org.overlord.apiman.dt.api.beans.idm.UserBean;
import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.OrganizationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource;
import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * CurrentUser resource proxy.
 * 
 * @author eric.wittmann@redhat.com
 */
public class FuseCurrentUserResource extends AbstractFuseResource<ICurrentUserResource> implements
        ICurrentUserResource {

    /**
     * Constructor.
     */
    public FuseCurrentUserResource() {
    }

    /**
     * @see org.overlord.apiman.dt.api.fuse6.jaxrs.AbstractFuseResource#getProxy()
     */
    @Override
    protected ICurrentUserResource getProxy() {
        return ServiceRegistryUtil.getSingleService(ICurrentUserResource.class);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource#getInfo()
     */
    @Override
    public UserBean getInfo() {
        return getProxy().getInfo();
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource#getOrganizations()
     */
    @Override
    public List<OrganizationSummaryBean> getOrganizations() {
        return getProxy().getOrganizations();
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource#getApplications()
     */
    @Override
    public List<ApplicationSummaryBean> getApplications() {
        return getProxy().getApplications();
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource#getServices()
     */
    @Override
    public List<ServiceSummaryBean> getServices() {
        return getProxy().getServices();
    }

}
