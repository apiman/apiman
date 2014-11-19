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
package org.overlord.apiman.engine.policies.auth;

import java.util.List;

import org.overlord.apiman.engine.policies.config.basicauth.StaticIdentity;
import org.overlord.apiman.engine.policies.config.basicauth.StaticIdentitySource;
import org.overlord.apiman.rt.engine.async.AsyncResultImpl;
import org.overlord.apiman.rt.engine.async.IAsyncResultHandler;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.policy.IPolicyContext;

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
     * @see org.overlord.apiman.engine.policies.auth.IIdentityValidator#validate(java.lang.String, java.lang.String, org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.policy.IPolicyContext, java.lang.Object, org.overlord.apiman.rt.engine.async.IAsyncHandler)
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
