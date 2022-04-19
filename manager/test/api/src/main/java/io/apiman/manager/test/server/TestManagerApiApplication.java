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
package io.apiman.manager.test.server;

import io.apiman.manager.api.rest.exceptions.mappers.BeanValidationExceptionMapper;
import io.apiman.manager.api.rest.exceptions.mappers.IllegalArgumentExceptionMapper;
import io.apiman.manager.api.rest.exceptions.mappers.RestExceptionMapper;
import io.apiman.manager.api.rest.impl.ActionResourceImpl;
import io.apiman.manager.api.rest.impl.ApiManagerApplication;
import io.apiman.manager.api.rest.impl.ApiResourceImpl;
import io.apiman.manager.api.rest.impl.BlobResourceImpl;
import io.apiman.manager.api.rest.impl.DeveloperPortalResourceImpl;
import io.apiman.manager.api.rest.impl.DeveloperResourceImpl;
import io.apiman.manager.api.rest.impl.DownloadResourceImpl;
import io.apiman.manager.api.rest.impl.EventResourceImpl;
import io.apiman.manager.api.rest.impl.GatewayResourceImpl;
import io.apiman.manager.api.rest.impl.OrganizationResourceImpl;
import io.apiman.manager.api.rest.impl.PluginResourceImpl;
import io.apiman.manager.api.rest.impl.PolicyDefinitionResourceImpl;
import io.apiman.manager.api.rest.impl.RoleResourceImpl;
import io.apiman.manager.api.rest.impl.SearchResourceImpl;
import io.apiman.manager.api.rest.impl.SystemResourceImpl;
import io.apiman.manager.api.rest.impl.UserResourceImpl;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

/**
 * JAX-RS {@link Application} used for testing.
 *
 * @author eric.wittmann@redhat.com
 */
public class TestManagerApiApplication extends ApiManagerApplication {

    private Set<Object> singletons = new HashSet<>();
    private Set<Class<?>> classes = new HashSet<>();

    /**
     * Constructor.
     */
    public TestManagerApiApplication() {
        classes.add(ApiResourceImpl.class);
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
        classes.add(DeveloperResourceImpl.class);
        classes.add(BlobResourceImpl.class);
        classes.add(EventResourceImpl.class);
        classes.add(DeveloperPortalResourceImpl.class);

        classes.add(RestExceptionMapper.class);
        classes.add(IllegalArgumentExceptionMapper.class);
        classes.add(BeanValidationExceptionMapper.class);
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
