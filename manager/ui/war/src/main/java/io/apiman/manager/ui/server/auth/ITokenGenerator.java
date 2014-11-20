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

import javax.servlet.http.HttpServletRequest;

/**
 * Simple interface used to generate bearer tokens.
 *
 * @author eric.wittmann@redhat.com
 */
public interface ITokenGenerator {

    /**
     * Generates a token.
     * @param request
     */
    public String generateToken(HttpServletRequest request);
    
    /**
     * Returns the time (in seconds) the client has before it should ask
     * for a new token.
     */
    public int getRefreshPeriod();
    
}
