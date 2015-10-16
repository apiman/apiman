/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.apiman.manager.api.micro;

import io.apiman.manager.api.config.Version;
import io.apiman.manager.api.rest.impl.ActionResourceImpl;
import io.apiman.manager.api.rest.impl.ApiManagerApplication;
import io.apiman.manager.api.rest.impl.CurrentUserResourceImpl;
import io.apiman.manager.api.rest.impl.GatewayResourceImpl;
import io.apiman.manager.api.rest.impl.OrganizationResourceImpl;
import io.apiman.manager.api.rest.impl.PermissionsResourceImpl;
import io.apiman.manager.api.rest.impl.PluginResourceImpl;
import io.apiman.manager.api.rest.impl.PolicyDefinitionResourceImpl;
import io.apiman.manager.api.rest.impl.RoleResourceImpl;
import io.apiman.manager.api.rest.impl.SearchResourceImpl;
import io.apiman.manager.api.rest.impl.SystemResourceImpl;
import io.apiman.manager.api.rest.impl.UserResourceImpl;
import io.apiman.manager.api.rest.impl.mappers.RestExceptionMapper;
import io.swagger.jaxrs.config.BeanConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * Useful if jax-rs is not supported by the runtime platform.
 *
 * @author eric.wittmann@redhat.com
 */
public class ManagerApiMicroServiceApplication extends ApiManagerApplication {

    private Set<Object> singletons = new HashSet<>();
    private Set<Class<?>> classes = new HashSet<>();

    /**
     * Constructor.
     */
    public ManagerApiMicroServiceApplication() {

        //add swagger 2.0 config
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(new Version().getVersionString());
        beanConfig.setBasePath("/apiman"); //$NON-NLS-1$
        beanConfig.setResourcePackage("io.apiman.manager.api.rest.contract"); //$NON-NLS-1$
        //TODO set more info in the beanConfig (title,description, host, port, etc)
        beanConfig.setScan(true);
        
        classes.add(SystemResourceImpl.class);
        classes.add(SearchResourceImpl.class);
        classes.add(RoleResourceImpl.class);
        classes.add(UserResourceImpl.class);
        classes.add(CurrentUserResourceImpl.class);
        classes.add(PermissionsResourceImpl.class);
        classes.add(OrganizationResourceImpl.class);
        classes.add(PolicyDefinitionResourceImpl.class);
        classes.add(GatewayResourceImpl.class);
        classes.add(PluginResourceImpl.class);
        classes.add(ActionResourceImpl.class);
        
        //add swagger 2.0 resource
        classes.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        classes.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        
        classes.add(RestExceptionMapper.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
