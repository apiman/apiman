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
package org.overlord.apiman.tools.devsvr.ui;

import javax.servlet.http.HttpServletRequest;

import org.overlord.apiman.dt.ui.server.auth.ITokenGenerator;
import org.overlord.commons.auth.util.SAMLAssertionUtil;

/**
 * Test token generator.  Uses SAML bearer token.  This could be promoted
 * to a non-test implementation if we ever decide to use SAML bearer token
 * outside of test.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiManDtUiTokenGenerator implements ITokenGenerator {

    private static final int TEN_MINUTES = 10 * 60 * 1000; // in millis
    private static final int NINE_MINUTES = 9 * 60; // in seconds

    /**
     * Constructor.
     */
    public ApiManDtUiTokenGenerator() {
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.auth.api.security.ITokenGenerator#generateToken(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public String generateToken(HttpServletRequest request) {
        // TODO parameterize the issuer and service
        String issuer = "apiman-dt-ui"; //$NON-NLS-1$
        String service = "/apiman-dt-api"; //$NON-NLS-1$
        return SAMLAssertionUtil.createSAMLAssertion(issuer, service, TEN_MINUTES);
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.auth.api.security.ITokenGenerator#getRefreshPeriod()
     */
    @Override
    public int getRefreshPeriod() {
        return NINE_MINUTES;
    }

}
