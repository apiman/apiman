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

import org.overlord.apiman.rt.engine.async.IAsyncResultHandler;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.policy.IPolicyContext;

/**
 * Interface used to validate an inbound user.
 * 
 * @author eric.wittmann@redhat.com
 */
public interface IIdentityValidator<C> {

    /**
     * Asynchronously validates a user.
     * @param username
     * @param password
     * @param request
     * @param context
     * @param config
     * @param handler
     */
    public void validate(String username, String password, ServiceRequest request, IPolicyContext context,
            C config, IAsyncResultHandler<Boolean> handler);

}
