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
package io.apiman.gateway.platforms.servlet;

import io.apiman.common.util.ApimanPathUtils.ApiRequestPathInfo;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class GatewayServletTest {

    /**
     * Test method for {@link io.apiman.gateway.platforms.servlet.GatewayServlet#parseApiRequestPath(java.lang.String)}.
     */
    @Test
    public void testParseApiRequestPath() {
        ApiRequestPathInfo info = parseApiRequestPath(null);

        info = parseApiRequestPath("/invalidpath");
        Assert.assertNull(info.orgId);
        Assert.assertNull(info.apiId);
        Assert.assertNull(info.apiVersion);
        Assert.assertNull(info.resource);

        info = parseApiRequestPath("/invalid/path");
        Assert.assertNull(info.orgId);
        Assert.assertNull(info.apiId);
        Assert.assertNull(info.apiVersion);
        Assert.assertNull(info.resource);

        info = parseApiRequestPath("/Org1/Api1/1.0");
        Assert.assertEquals("Org1", info.orgId);
        Assert.assertEquals("Api1", info.apiId);
        Assert.assertEquals("1.0", info.apiVersion);
        Assert.assertNull(info.resource);

        info = parseApiRequestPath("/MyOrg/Api-99/2.7");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Api-99", info.apiId);
        Assert.assertEquals("2.7", info.apiVersion);
        Assert.assertNull(info.resource);

        info = parseApiRequestPath("/MyOrg/Api-99/2.7/resource");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Api-99", info.apiId);
        Assert.assertEquals("2.7", info.apiVersion);
        Assert.assertEquals("/resource", info.resource);

        info = parseApiRequestPath("/MyOrg/Api-99/2.7/path/to/resource");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Api-99", info.apiId);
        Assert.assertEquals("2.7", info.apiVersion);
        Assert.assertEquals("/path/to/resource", info.resource);

        info = parseApiRequestPath("/MyOrg/Api-99/2.7/path/to/resource?query=1234");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Api-99", info.apiId);
        Assert.assertEquals("2.7", info.apiVersion);
        Assert.assertEquals("/path/to/resource?query=1234", info.resource);

        info = parseApiRequestPath("/MyOrg/Api-99/path/to/resource?query=1234", null, "2.7");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Api-99", info.apiId);
        Assert.assertEquals("2.7", info.apiVersion);
        Assert.assertEquals("/path/to/resource?query=1234", info.resource);

        info = parseApiRequestPath("/MyOrg/Api-99/path/to/resource?query=1234", "application/apiman.2.7+json", null);
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Api-99", info.apiId);
        Assert.assertEquals("2.7", info.apiVersion);
        Assert.assertEquals("/path/to/resource?query=1234", info.resource);

    }

    /**
     * @param path
     */
    private ApiRequestPathInfo parseApiRequestPath(String path) {
        return parseApiRequestPath(path, null, null);
    }

    /**
     * @param path
     * @param acceptHeader
     * @param apiVersionHeader
     */
    private ApiRequestPathInfo parseApiRequestPath(String path, String acceptHeader, String apiVersionHeader) {
        MockHttpServletRequest mockReq = new MockHttpServletRequest(path);
        if (acceptHeader != null) {
            mockReq.setHeader("Accept", acceptHeader);
        }
        if (apiVersionHeader != null) {
            mockReq.setHeader("X-API-Version", apiVersionHeader);
        }
        return GatewayServlet.parseApiRequestPath(mockReq);
    }

    /**
     * Test method for {@link io.apiman.gateway.platforms.servlet.GatewayServlet#parseApiRequestQueryParameters(String)}
     */
    @Test
    public void testParseApiRequestQueryParams() {
        Map<String, List<String>> paramMap = GatewayServlet.parseApiRequestQueryParameters(null);
        Assert.assertNotNull(paramMap);

        paramMap = GatewayServlet.parseApiRequestQueryParameters("param1");
        Assert.assertEquals(1, paramMap.get("param1").size());
        Assert.assertNull(paramMap.get("param1").get(0));

        paramMap = GatewayServlet.parseApiRequestQueryParameters("param1=value1");
        Assert.assertEquals(1, paramMap.get("param1").size());
        Assert.assertEquals("value1", paramMap.get("param1").get(0));
        
        paramMap = GatewayServlet.parseApiRequestQueryParameters("param1=value1&param1=value2");
        Assert.assertEquals(2, paramMap.get("param1").size());
        Assert.assertEquals("value1", paramMap.get("param1").get(0));
        Assert.assertEquals("value2", paramMap.get("param1").get(1));

        paramMap = GatewayServlet.parseApiRequestQueryParameters("param1=value1&param2");
        Assert.assertEquals(1, paramMap.get("param1").size());
        Assert.assertEquals("value1", paramMap.get("param1").get(0));
        Assert.assertEquals(1, paramMap.get("param2").size());
        Assert.assertNull(paramMap.get("param2").get(0));
        
        paramMap = GatewayServlet.parseApiRequestQueryParameters("param2=");
        Assert.assertEquals(1, paramMap.get("param2").size());
        Assert.assertEquals("", paramMap.get("param2").get(0));
        
        paramMap = GatewayServlet.parseApiRequestQueryParameters("param2&param2=&param2=value1&param2&param2=");
        Assert.assertEquals(5, paramMap.get("param2").size());
        Assert.assertNull(paramMap.get("param2").get(0));
        Assert.assertEquals("", paramMap.get("param2").get(1));
        Assert.assertEquals("value1", paramMap.get("param2").get(2));
        Assert.assertNull(paramMap.get("param2").get(3));
        Assert.assertEquals("", paramMap.get("param2").get(4));

        paramMap = GatewayServlet.parseApiRequestQueryParameters("param1=value1&param2=value2");
        Assert.assertEquals(1, paramMap.get("param1").size());
        Assert.assertEquals("value1", paramMap.get("param1").get(0));
        Assert.assertEquals(1, paramMap.get("param2").size());
        Assert.assertEquals("value2", paramMap.get("param2").get(0));
        
        paramMap = GatewayServlet.parseApiRequestQueryParameters("param%201=hello%20world&param+2=hello+world&param+3=%3D%2B%26%3F");
        Assert.assertEquals("hello world", paramMap.get("param 1").get(0));
        Assert.assertEquals("hello world", paramMap.get("param 2").get(0));
        Assert.assertEquals("=+&?", paramMap.get("param 3").get(0));
    }

}
