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
package io.apiman.gateway.engine.policies.auth;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.policies.config.basicauth.StaticIdentity;
import io.apiman.gateway.engine.policies.config.basicauth.StaticIdentitySource;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.util.List;

/**
 * An identity validator that uses the static information in the config
 * to validate the user.
 *
 * @author eric.wittmann@redhat.com
 */
public class StaticIdentityValidator implements IIdentityValidator<StaticIdentitySource> {
    
    /**
     * Constructor.
     */
    public StaticIdentityValidator() {
    }

    /**
     * @see io.apiman.gateway.engine.policies.auth.IIdentityValidator#validate(java.lang.String, java.lang.String, io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void validate(String username, String password, ServiceRequest request, IPolicyContext context,
            StaticIdentitySource config, IAsyncResultHandler<Boolean> handler) {
        List<StaticIdentity> identities = config.getIdentities();
        for (StaticIdentity identity : identities) {
            if (identity.getUsername().equals(username) && identity.getPassword().equals(password)) {
                handler.handle(AsyncResultImpl.create(Boolean.TRUE));
                return;
            }
        }
        handler.handle(AsyncResultImpl.create(Boolean.FALSE));
    }

}
