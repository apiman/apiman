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
package io.apiman.manager.ui.client.shared.services;

import io.apiman.manager.ui.client.shared.beans.BearerTokenCredentialsBean;

import org.jboss.errai.bus.server.annotations.Remote;

/**
 * Provides a way to refresh a bearer token.
 *
 * @author eric.wittmann@redhat.com
 */
@Remote
public interface ITokenRefreshService {
    
    /**
     * Called to create a new bearer token.
     */
    public BearerTokenCredentialsBean refreshToken();

}
