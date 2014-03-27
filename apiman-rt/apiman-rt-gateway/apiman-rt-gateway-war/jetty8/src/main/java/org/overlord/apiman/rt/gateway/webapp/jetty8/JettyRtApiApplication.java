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
package org.overlord.apiman.rt.gateway.webapp.jetty8;

import java.util.HashSet;
import java.util.Set;

import org.overlord.apiman.rt.api.rest.impl.ApplicationResourceImpl;
import org.overlord.apiman.rt.api.rest.impl.RtApiApplication;
import org.overlord.apiman.rt.api.rest.impl.ServiceResourceImpl;
import org.overlord.apiman.rt.api.rest.impl.mappers.RestExceptionMapper;

/**
 * Useful if jax-rs is not supported by the runtime platform.
 *
 * @author eric.wittmann@redhat.com
 */
public class JettyRtApiApplication extends RtApiApplication {

    private Set<Object> singletons = new HashSet<Object>();
    private Set<Class<?>> classes = new HashSet<Class<?>>();

    /**
     * Constructor.
     */
    public JettyRtApiApplication() {
        classes.add(ApplicationResourceImpl.class);
        classes.add(ServiceResourceImpl.class);
        
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
