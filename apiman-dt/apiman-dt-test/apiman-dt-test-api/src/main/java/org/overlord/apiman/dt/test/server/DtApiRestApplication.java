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

package org.overlord.apiman.dt.test.server;

import java.util.HashSet;
import java.util.Set;

import org.overlord.apiman.dt.api.rest.ApiManDtApplication;
import org.overlord.apiman.dt.api.rest.impl.OrganizationResourceImpl;
import org.overlord.apiman.dt.api.rest.impl.PermissionsResourceImpl;
import org.overlord.apiman.dt.api.rest.impl.RoleResourceImpl;
import org.overlord.apiman.dt.api.rest.impl.SystemResourceImpl;
import org.overlord.apiman.dt.api.rest.impl.UserResourceImpl;
import org.overlord.apiman.dt.api.rest.impl.mappers.RestExceptionMapper;

/**
 * Version of the application used for testing.
 *
 * @author eric.wittmann@redhat.com
 */
public class DtApiRestApplication extends ApiManDtApplication {

    private Set<Object> singletons = new HashSet<Object>();
    private Set<Class<?>> classes = new HashSet<Class<?>>();

    /**
     * Constructor.
     */
    public DtApiRestApplication() {
        classes.add(SystemResourceImpl.class);
        classes.add(RoleResourceImpl.class);
        classes.add(UserResourceImpl.class);
        classes.add(PermissionsResourceImpl.class);
        classes.add(OrganizationResourceImpl.class);
        
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
