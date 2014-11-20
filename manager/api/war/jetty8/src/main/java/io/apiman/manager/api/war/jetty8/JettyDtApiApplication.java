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
package io.apiman.manager.api.war.jetty8;

import java.util.HashSet;
import java.util.Set;

import io.apiman.manager.api.rest.impl.ActionResourceImpl;
import io.apiman.manager.api.rest.impl.CurrentUserResourceImpl;
import io.apiman.manager.api.rest.impl.DtApiApplication;
import io.apiman.manager.api.rest.impl.OrganizationResourceImpl;
import io.apiman.manager.api.rest.impl.PermissionsResourceImpl;
import io.apiman.manager.api.rest.impl.PolicyDefinitionResourceImpl;
import io.apiman.manager.api.rest.impl.RoleResourceImpl;
import io.apiman.manager.api.rest.impl.SearchResourceImpl;
import io.apiman.manager.api.rest.impl.SystemResourceImpl;
import io.apiman.manager.api.rest.impl.UserResourceImpl;
import io.apiman.manager.api.rest.impl.mappers.RestExceptionMapper;

/**
 * Useful if jax-rs is not supported by the runtime platform.
 *
 * @author eric.wittmann@redhat.com
 */
public class JettyDtApiApplication extends DtApiApplication {

    private Set<Object> singletons = new HashSet<Object>();
    private Set<Class<?>> classes = new HashSet<Class<?>>();

    /**
     * Constructor.
     */
    public JettyDtApiApplication() {
        classes.add(SystemResourceImpl.class);
        classes.add(SearchResourceImpl.class);
        classes.add(RoleResourceImpl.class);
        classes.add(UserResourceImpl.class);
        classes.add(CurrentUserResourceImpl.class);
        classes.add(PermissionsResourceImpl.class);
        classes.add(OrganizationResourceImpl.class);
        classes.add(PolicyDefinitionResourceImpl.class);
        classes.add(ActionResourceImpl.class);

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
