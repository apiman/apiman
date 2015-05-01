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
package io.apiman.gateway.engine.policies.config.basicauth;

import io.apiman.gateway.engine.policies.BasicAuthenticationPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple static set of identities used to perform basic authentication
 * by the {@link BasicAuthenticationPolicy} implementation.  This is likely
 * not something that will be used in production as it is very insecure.  It
 * is mostly useful for testing.
 *
 * @author eric.wittmann@redhat.com
 */
public class StaticIdentitySource {

    private List<StaticIdentity> identities = new ArrayList<>();

    /**
     * Constructor.
     */
    public StaticIdentitySource() {
    }

    /**
     * @return the identities
     */
    public List<StaticIdentity> getIdentities() {
        return identities;
    }

    /**
     * @param identities the identities to set
     */
    public void setIdentities(List<StaticIdentity> identities) {
        this.identities = identities;
    }

}
