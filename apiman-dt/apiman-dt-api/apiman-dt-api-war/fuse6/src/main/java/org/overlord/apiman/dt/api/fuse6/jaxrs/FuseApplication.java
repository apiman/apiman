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

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.overlord.apiman.dt.api.fuse6.auth.AuthTokenRequestHandler;
import org.overlord.apiman.dt.api.fuse6.auth.AuthTokenResponseHandler;
import org.overlord.apiman.dt.api.rest.contract.IActionResource;
import org.overlord.apiman.dt.api.rest.contract.ICurrentUserResource;
import org.overlord.apiman.dt.api.rest.contract.IOrganizationResource;
import org.overlord.apiman.dt.api.rest.contract.IPermissionsResource;
import org.overlord.apiman.dt.api.rest.contract.IPolicyDefinitionResource;
import org.overlord.apiman.dt.api.rest.contract.IRoleResource;
import org.overlord.apiman.dt.api.rest.contract.ISearchResource;
import org.overlord.apiman.dt.api.rest.contract.ISystemResource;
import org.overlord.apiman.dt.api.rest.contract.IUserResource;
import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * A jax-rs application used when running the API in fuse.
 *
 * @author eric.wittmann@redhat.com
 */
public class FuseApplication extends Application {
    
    /**
     * Constructor.
     */
    public FuseApplication() {
    }
    
    /**
     * @see javax.ws.rs.core.Application#getClasses()
     */
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(AuthTokenRequestHandler.class);
        classes.add(AuthTokenResponseHandler.class);
        classes.add(JacksonJsonProvider.class);
        return super.getClasses();
    }
    
    /**
     * @see javax.ws.rs.core.Application#getSingletons()
     */
    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<Object>();
        addResourceTo(IActionResource.class, singletons);
        addResourceTo(ICurrentUserResource.class, singletons);
        addResourceTo(IOrganizationResource.class, singletons);
        addResourceTo(IPermissionsResource.class, singletons);
        addResourceTo(IPolicyDefinitionResource.class, singletons);
        addResourceTo(IRoleResource.class, singletons);
        addResourceTo(ISearchResource.class, singletons);
        addResourceTo(ISystemResource.class, singletons);
        addResourceTo(IUserResource.class, singletons);
        return singletons;
    }

    /**
     * Gets the resource from the osgi registry.
     * @param resourceInterface
     * @param singletons
     */
    private void addResourceTo(Class<?> resourceInterface, Set<Object> singletons) {
        Object resource = ServiceRegistryUtil.getSingleService(resourceInterface);
        if (resource != null) {
            singletons.add(resource);
        }
    }

}
