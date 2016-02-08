/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.plugins.cors_policy;

import static org.mockito.BDDMockito.given;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Common test base
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class CorsConnectorTestBase {

    protected IPolicyFailureFactoryComponent failureFactory = new IPolicyFailureFactoryComponent() {

        @Override
        public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
           return new PolicyFailure(type, failureCode, message);
        }
    };

    @Mock
    protected ApiRequest request;
    protected CorsConfigBean config;
    protected CorsConnector connector;

    protected Set<String> allowHeaders;
    protected Set<String> allowMethods;
    protected Set<String> allowOrigins;
    protected Set<String> exposeHeaders;

    protected HeaderMap requestHeaders = new HeaderMap();

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);

        config = new CorsConfigBean();

        allowHeaders = config.getAllowHeaders();
        allowMethods = config.getAllowMethods();
        allowOrigins = config.getAllowOrigin();
        exposeHeaders = config.getExposeHeaders();

        // Match defaults in json-schema
        config.setAllowCredentials(false);
        config.setErrorOnCorsFailure(true);
        config.setMaxAge(0);

        given(request.getHeaders()).willReturn(requestHeaders);
    }

    protected void setOrigin(String origin) {
        requestHeaders.put(CorsConnector.ORIGIN_KEY, origin);
    }

    protected void setHost(String host) {
        requestHeaders.put(CorsConnector.HOST_KEY, host);
    }

    protected void setContentType(String type) {
        requestHeaders.put(CorsConnector.CONTENT_TYPE, type);
    }

    protected void setRequestMethods(String... methods) {
        requestHeaders.put(CorsConnector.AC_REQUEST_METHOD_KEY, StringUtils.join(methods, ", "));
    }

    protected void setRequestHeaders(String... headers) {
        requestHeaders.put(CorsConnector.AC_REQUEST_HEADERS_KEY, StringUtils.join(headers, ", "));
    }
}
