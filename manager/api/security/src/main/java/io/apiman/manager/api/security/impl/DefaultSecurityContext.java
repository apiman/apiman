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
package io.apiman.manager.api.security.impl;

import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.beans.idm.UserDto;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * The basic/default implementation of a security context.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped @Alternative
public class DefaultSecurityContext extends AbstractSecurityContext {

    private IStorage iStorage;
    // private ApiManagerConfig config;

    /**
     * Constructor.
     */
    @Inject
    public DefaultSecurityContext(IStorage iStorage) {
        this.iStorage = iStorage;
    }

    public DefaultSecurityContext() {
    }

    /**
     * Called to set the current context http servlet request.
     *
     * @param request
     */
    protected static void setServletRequest(HttpServletRequest request) {
        servletRequest.set(request);
    }

    /**
     * Called to clear the current thread local permissions bean.
     */
    protected static void clearPermissions() {
        AbstractSecurityContext.clearPermissions();
    }

    @Override
    public Locale getLocale() {
        return servletRequest.get().getLocale();
    }

    /**
     * Called to clear the context http servlet request.
     */
    protected static void clearServletRequest() {
        servletRequest.remove();
    }

    @Override
    public List<UserDto> getRemoteUsersWithRole(String roleName) {
        return Collections.emptyList();
    }
}
