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

import org.overlord.apiman.dt.api.beans.policies.PolicyDefinitionBean;
import org.overlord.apiman.dt.api.rest.contract.IPolicyDefinitionResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyDefinitionAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyDefinitionNotFoundException;
import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * PolicyDefinition resource proxy.
 *
 * @author eric.wittmann@redhat.com
 */
public class FusePolicyDefinitionResource extends AbstractFuseResource<IPolicyDefinitionResource> implements IPolicyDefinitionResource {
    
    /**
     * Constructor.
     */
    public FusePolicyDefinitionResource() {
    }

    /**
     * @see org.overlord.apiman.dt.api.fuse6.jaxrs.AbstractFuseResource#getProxy()
     */
    @Override
    protected IPolicyDefinitionResource getProxy() {
        return ServiceRegistryUtil.getSingleService(IPolicyDefinitionResource.class);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPolicyDefinitionResource#list()
     */
    @Override
    public List<PolicyDefinitionBean> list() throws NotAuthorizedException {
        return getProxy().list();
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPolicyDefinitionResource#create(org.overlord.apiman.dt.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public PolicyDefinitionBean create(PolicyDefinitionBean bean)
            throws PolicyDefinitionAlreadyExistsException, NotAuthorizedException {
        return getProxy().create(bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPolicyDefinitionResource#get(java.lang.String)
     */
    @Override
    public PolicyDefinitionBean get(String policyDefinitionId) throws PolicyDefinitionNotFoundException,
            NotAuthorizedException {
        return getProxy().get(policyDefinitionId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPolicyDefinitionResource#update(java.lang.String, org.overlord.apiman.dt.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void update(String policyDefinitionId, PolicyDefinitionBean bean)
            throws PolicyDefinitionNotFoundException, NotAuthorizedException {
        getProxy().update(policyDefinitionId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPolicyDefinitionResource#delete(java.lang.String)
     */
    @Override
    public void delete(String policyDefinitionId) throws PolicyDefinitionNotFoundException,
            NotAuthorizedException {
        getProxy().delete(policyDefinitionId);
    }

}
