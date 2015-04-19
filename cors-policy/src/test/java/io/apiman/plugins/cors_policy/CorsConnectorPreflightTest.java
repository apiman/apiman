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

import java.util.Map;

import io.apiman.plugins.cors_policy.util.HttpHelper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test preflight aspects of CORS
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class CorsConnectorPreflightTest extends CorsConnectorTestBase {

    @Before
    @Override
    public void before() {
        super.before();
        given(request.getType()).willReturn(HttpHelper.OPTIONS);
    }
    
    @Test
    public void shouldSucceedWithMandatoryFields() {
        allowOrigins.add("bede");
        config.getAllowMethods().add("GET");

        setOrigin("bede");
        setHost("venerable");
        
        setRequestMethods(new String[] { "GET" });

        connector = new CorsConnector(request, config, failureFactory);
        Map<String, String> responseHeaders = connector.getResponseHeaders();

        Assert.assertTrue(!connector.isFailure());
        Assert.assertTrue(connector.isShortcircuit());
        Assert.assertEquals("bede", responseHeaders.get(CorsConnector.AC_ALLOW_ORIGIN_KEY));
        Assert.assertEquals("GET", responseHeaders.get(CorsConnector.AC_ALLOW_METHODS_KEY));
    }
    
    @Test
    public void shouldFailWithoutMandatoryFields() {
        allowOrigins.add("bede");
        
        setOrigin("bede");
        setHost("venerable");
        
        connector = new CorsConnector(request, config, failureFactory);

        Assert.assertTrue(connector.isFailure());
        Assert.assertTrue(!connector.isShortcircuit());
    }
    
    @Test
    public void shouldFailWithDisallowedMethods() {
        allowOrigins.add("bede");
        config.getAllowMethods().add("GET, HEAD");

        setOrigin("bede");
        setHost("venerable");
        
        setRequestMethods(new String[] { "DELETE" });

        connector = new CorsConnector(request, config, failureFactory);
        Map<String, String> responseHeaders = connector.getResponseHeaders();

        Assert.assertTrue(connector.isFailure());
        Assert.assertEquals(400, connector.getFailure().getResponseCode());
        Assert.assertTrue(!connector.isShortcircuit());
        Assert.assertEquals("bede", responseHeaders.get(CorsConnector.AC_ALLOW_ORIGIN_KEY));
        // Must still list allowed methods
        Assert.assertEquals("GET, HEAD", responseHeaders.get(CorsConnector.AC_ALLOW_METHODS_KEY));
    }
    
    @Test
    public void shouldSucceedWithAllowedHeaders() {
        commonSetup();
        
        allowHeaders.add("x-penshaw");
        allowHeaders.add("x-monument");
        
        setRequestHeaders(new String[] { "x-monument", "x-penshaw" } );
        
        connector = new CorsConnector(request, config, failureFactory);
        Map<String, String> responseHeaders = connector.getResponseHeaders();
        
        Assert.assertTrue(!connector.isFailure());
        Assert.assertTrue(connector.isShortcircuit());
        Assert.assertEquals("x-penshaw, x-monument", 
                responseHeaders.get(CorsConnector.AC_ALLOW_HEADERS_KEY));
    }
    
    @Test
    public void shouldFailWithDisallowedHeaders() {
        commonSetup();
        
        allowHeaders.add("x-monument");
        
        setRequestHeaders(new String[] { "x-penshaw", "x-monument" } );
        
        connector = new CorsConnector(request, config, failureFactory);
        Map<String, String> responseHeaders = connector.getResponseHeaders();
        
        Assert.assertTrue(connector.isFailure());
        Assert.assertTrue(!connector.isShortcircuit());
        Assert.assertEquals(null, 
                responseHeaders.get(CorsConnector.AC_ALLOW_HEADERS_KEY));       
    }
    
    @Test
    public void shouldSetMaxAgeHeaderIfConfigSet() {
        commonSetup();
        
        config.setMaxAge(9001);
        
        connector = new CorsConnector(request, config, failureFactory);
        Map<String, String> responseHeaders = connector.getResponseHeaders();
        
        Assert.assertTrue(!connector.isFailure());
        Assert.assertTrue(connector.isShortcircuit());
        Assert.assertEquals("9001", responseHeaders.get(CorsConnector.AC_MAX_AGE_KEY));
    }

    private void commonSetup() {
        allowOrigins.add("bede");
        config.getAllowMethods().add("GET");

        setOrigin("bede");
        setHost("venerable");
        
        setRequestMethods(new String[] { "GET" });
    }
}
