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

import static org.mockito.BDDMockito.*;

import java.util.Map;

import io.apiman.plugins.cors_policy.util.HttpHelper;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test simple aspects of CORS
 * 
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class CorsConnectorSimpleTest extends CorsConnectorTestBase {

    @Test
    public void shouldFailWithForbiddenOrigin() {
        given(request.getType()).willReturn(HttpHelper.GET);

        setOrigin("http://example.com");
        setHost("http://wibble.com");

        allowOrigins.add("allowedOrigin");

        connector = new CorsConnector(request, config, failureFactory);

        Assert.assertTrue(connector.isFailure());
    }

    @Test
    public void shouldAllowAnyOriginWithStar() {
        given(request.getType()).willReturn(HttpHelper.GET);

        setOrigin("blergs");
        setHost("123");

        allowOrigins.add("*");
        allowOrigins.add("thiswillbeignored");

        connector = new CorsConnector(request, config, failureFactory);
        Map<String, String> responseHeaders = connector.getResponseHeaders();

        Assert.assertTrue(!connector.isFailure());
        Assert.assertEquals("blergs", responseHeaders.get(CorsConnector.AC_ALLOW_ORIGIN_KEY));
    }
    
    // Some browsers include Origin even with some cases where host == origin
    @Test
    public void shouldAllowWhenOriginMatchesHost() {
        given(request.getType()).willReturn(HttpHelper.GET);

        setOrigin("panacalty");
        setHost("panacalty");

        allowOrigins.add("someotherorigin");

        connector = new CorsConnector(request, config, failureFactory);
        Map<String, String> responseHeaders = connector.getResponseHeaders();

        Assert.assertTrue(!connector.isFailure());
        Assert.assertEquals("panacalty", responseHeaders.get(CorsConnector.AC_ALLOW_ORIGIN_KEY));       
    }

    @Test
    public void shouldAllowGetWithAllowedOrigin() {
        given(request.getType()).willReturn(HttpHelper.GET);

        setOrigin("http://example.com");
        setHost("http://wibble.com");

        allowOrigins.add("http://example.com");

        connector = new CorsConnector(request, config, failureFactory);
        Map<String, String> responseHeaders = connector.getResponseHeaders();

        Assert.assertTrue(!connector.isFailure());
        Assert.assertEquals("http://example.com", responseHeaders.get(CorsConnector.AC_ALLOW_ORIGIN_KEY));
    }

    @Test
    public void shouldAllowHeadWithAllowedOrigin() {
        given(request.getType()).willReturn(HttpHelper.HEAD);

        setOrigin("http://example.com");
        setHost("http://wibble.com");

        allowOrigins.add("http://example.com");

        connector = new CorsConnector(request, config, failureFactory);

        Assert.assertTrue(!connector.isFailure());
    }

    @Test
    public void shouldAllowPostWithAllowedOriginAndContentType() {
        given(request.getType()).willReturn(HttpHelper.POST);

        setOrigin("http://example.com");
        setHost("http://wibble.com");

        allowOrigins.add("http://example.com");

        setContentType("text/plain");

        connector = new CorsConnector(request, config, failureFactory);

        Assert.assertTrue(!connector.isFailure());
    }

    @Test
    public void shouldForbidPostWithComplexContentType() {
        given(request.getType()).willReturn(HttpHelper.POST);

        setOrigin("http://example.com");
        setHost("http://wibble.com");

        allowOrigins.add("http://example.com");
        // This type is NOT a simple contentType,
        // hence should be rejected - must use preflight.
        setContentType("text/json");

        connector = new CorsConnector(request, config, failureFactory);

        Assert.assertTrue(connector.isFailure());
    }

    @Test
    public void shouldForbidNonSimpleVerbs() {
        given(request.getType()).willReturn("PUT");

        setOrigin("http://example.com");
        setHost("http://wibble.com");

        allowOrigins.add("http://example.com");

        connector = new CorsConnector(request, config, failureFactory);

        Assert.assertTrue(connector.isFailure());
        Assert.assertEquals("Invalid simple request", 400, connector.getFailure().getFailureCode());
    }

    @Test
    public void shouldInsertHeadersInExposeList() {
        given(request.getType()).willReturn("GET");

        exposeHeaders.add("Vindolanda");
        exposeHeaders.add("Lindisfarne");

        allowOrigins.add("http://example.com");

        setOrigin("http://example.com");
        setHost("http://wibble.com");

        connector = new CorsConnector(request, config, failureFactory);
        Map<String, String> responseHeaders = connector.getResponseHeaders();

        Assert.assertTrue(!connector.isFailure());
        Assert.assertEquals("Vindolanda, Lindisfarne",
                responseHeaders.get(CorsConnector.AC_EXPOSE_HEADERS_KEY));
    }

    @Test
    public void shouldEchoMatchingOrigin() {
        given(request.getType()).willReturn("GET");

        allowOrigins.add("betrand");
        allowOrigins.add("russell");
        allowOrigins.add("emmanuel");
        allowOrigins.add("kant");

        setOrigin("russell");
        setHost("philosopher");

        connector = new CorsConnector(request, config, failureFactory);
        Map<String, String> responseHeaders = connector.getResponseHeaders();

        Assert.assertTrue(!connector.isFailure());
        Assert.assertEquals("russell", responseHeaders.get(CorsConnector.AC_ALLOW_ORIGIN_KEY));
    }

    @Test
    public void shouldAllowCredentialsIfSet() {
        given(request.getType()).willReturn("GET");

        allowOrigins.add("mellifluous");

        setOrigin("mellifluous");
        setHost("honey");

        // Should set allow credentials header
        config.setAllowCredentials(true);

        connector = new CorsConnector(request, config, failureFactory);
        Map<String, String> responseHeaders = connector.getResponseHeaders();

        Assert.assertTrue(!connector.isFailure());
        Assert.assertEquals("true", responseHeaders.get(CorsConnector.AC_ALLOW_CREDENTIALS_KEY));
    }
    
    @Test
    public void shouldNotSetCredentialsHeaderIfUnset() {
        given(request.getType()).willReturn("GET");

        allowOrigins.add("stottie");

        setOrigin("stottie");
        setHost("bap");

        // This is default value, but just to be clear...
        config.setAllowCredentials(false);

        connector = new CorsConnector(request, config, failureFactory);
        Map<String, String> responseHeaders = connector.getResponseHeaders();

        Assert.assertTrue(!connector.isFailure());
        // Header should not be set at all if allowCredentials is false.
        Assert.assertEquals(null, responseHeaders.get(CorsConnector.AC_ALLOW_CREDENTIALS_KEY));        
    }
}
