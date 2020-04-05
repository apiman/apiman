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

package io.apiman.manager.api.rest.impl;

import io.apiman.manager.api.config.Version;
import io.apiman.manager.api.exportimport.manager.ExportImportManager;
import io.apiman.manager.api.rest.exceptions.mappers.RestExceptionMapper;
import io.swagger.jaxrs.config.BeanConfig;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;


/**
 * The jax-rs application for the API Manager rest api.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationPath("/")
@ApplicationScoped
public class ApiManagerApplication extends Application {

    @Inject
    ExportImportManager manager;

    private Set<Class<?>> classes = new HashSet<>();

    /**
     * Constructor.
     */
    public ApiManagerApplication() {
        //add swagger 2.0 config
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(new Version().getVersionString());
        beanConfig.setBasePath("/apiman"); //$NON-NLS-1$
        beanConfig.setResourcePackage("io.apiman.manager.api.rest"); //$NON-NLS-1$
        beanConfig.setTitle("API Manager REST API");
        beanConfig.setDescription("The API Manager REST API is used by the API Manager UI to get stuff done. You can use it to automate any API Management task you wish. For example, create new Organizations, Plans, Clients, and APIs.");
        beanConfig.setScan(true);

        classes.add(SystemResourceImpl.class);
        classes.add(SearchResourceImpl.class);
        classes.add(RoleResourceImpl.class);
        classes.add(UserResourceImpl.class);
        classes.add(OrganizationResourceImpl.class);
        classes.add(PolicyDefinitionResourceImpl.class);
        classes.add(GatewayResourceImpl.class);
        classes.add(PluginResourceImpl.class);
        classes.add(ActionResourceImpl.class);
        classes.add(DownloadResourceImpl.class);

        //add swagger 2.0 resource
        classes.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        classes.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        classes.add(RestExceptionMapper.class);
    }
    
    @PostConstruct
    protected void postConstruct() {
        if (manager.isImportExport()) {
            manager.doImportExport();
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
