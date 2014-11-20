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
package io.apiman.manager.ui.server.auth;

import io.apiman.common.auth.AuthTokenUtil;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * A simple token generator that uses {@link AuthToken} auth.  This is something specific
 * to API Man and is used for testing and demos.  In any production environment something
 * like OAuth or some other bearer token authentication should be used between the UI
 * and the management API.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuthTokenGenerator implements ITokenGenerator {
    
    private static final int TEN_MINUTES = 10 * 60 * 1000; // in millis
    private static final int NINE_MINUTES = 9 * 60; // in seconds

    /**
     * Constructor.
     */
    public AuthTokenGenerator() {
    }

    /**
     * @see io.apiman.manager.ui.server.auth.api.security.ITokenGenerator#generateToken(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public String generateToken(HttpServletRequest request) {
        String principal = request.getRemoteUser();
        // TODO create platform specific subclasses of this to get the roles properly
        Set<String> roles = new HashSet<String>();
        roles.add("apiuser"); //$NON-NLS-1$
        if (request.isUserInRole("apiadmin")) { //$NON-NLS-1$
            roles.add("apiadmin"); //$NON-NLS-1$
        }
        return AuthTokenUtil.produceToken(principal, roles, TEN_MINUTES);
    }

    /**
     * @see io.apiman.manager.ui.server.auth.api.security.ITokenGenerator#getRefreshPeriod()
     */
    @Override
    public int getRefreshPeriod() {
        return NINE_MINUTES;
    }

    

}
