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

import java.util.Set;

import org.overlord.commons.auth.filters.SamlBearerTokenAuthFilter;

/**
 * Auth filter for the test server.
 *
 * @author eric.wittmann@redhat.com
 */
public class DtApiTestAuthFilter extends SamlBearerTokenAuthFilter {

    /**
     * Constructor.
     */
    public DtApiTestAuthFilter() {
    }

    /**
     * @see org.overlord.commons.auth.filters.SamlBearerTokenAuthFilter#defaultAllowedIssuers()
     */
    @Override
    protected Set<String> defaultAllowedIssuers() {
        return null;
    }

    /**
     * @see org.overlord.commons.auth.filters.SamlBearerTokenAuthFilter#defaultSignatureRequired()
     */
    @Override
    protected boolean defaultSignatureRequired() {
        return false;
    }

}
