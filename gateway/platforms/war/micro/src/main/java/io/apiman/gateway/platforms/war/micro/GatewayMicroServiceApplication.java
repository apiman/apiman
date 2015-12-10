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
package io.apiman.gateway.platforms.war.micro;

import io.apiman.gateway.api.rest.impl.ClientResourceImpl;
import io.apiman.gateway.api.rest.impl.GatewayApiApplication;
import io.apiman.gateway.api.rest.impl.ApiResourceImpl;
import io.apiman.gateway.api.rest.impl.SystemResourceImpl;
import io.apiman.gateway.api.rest.impl.mappers.RestExceptionMapper;
import io.swagger.jaxrs.config.BeanConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * Useful if jax-rs is not supported by the runtime platform.
 *
 * @author eric.wittmann@redhat.com
 */
public class GatewayMicroServiceApplication extends GatewayApiApplication {

    private Set<Object> singletons = new HashSet<>();
    private Set<Class<?>> classes = new HashSet<>();

    /**
     * Constructor.
     */
    public GatewayMicroServiceApplication() {
        //add swagger 2.0 config
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setBasePath("/api"); //$NON-NLS-1$
        beanConfig.setResourcePackage("io.apiman.gateway.api.rest.contract"); //$NON-NLS-1$
        //TODO set more info in the beanConfig (title,description, version, etc)
        beanConfig.setScan(true);
        
        classes.add(SystemResourceImpl.class);
        classes.add(ApiResourceImpl.class);
        classes.add(ClientResourceImpl.class);

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
