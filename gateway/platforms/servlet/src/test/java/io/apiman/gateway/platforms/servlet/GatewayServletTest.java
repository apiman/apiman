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
import io.apiman.gateway.engine.beans.util.QueryMap;

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
     * Test method for {@link io.apiman.gateway.platforms.servlet.GatewayServlet#parseApiRequestPath(javax.servlet.http.HttpServletRequest)}.
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
     * Test method for {@link io.apiman.gateway.platforms.servlet.GatewayServlet#parseApiRequestQueryParams(String)}
     */
    @Test
    public void testParseApiRequestQueryParams() {
        QueryMap paramMap = GatewayServlet.parseApiRequestQueryParams(null);
        Assert.assertNotNull(paramMap);

        paramMap = GatewayServlet.parseApiRequestQueryParams("param1");
        Assert.assertNull(paramMap.get("param1"));

        paramMap = GatewayServlet.parseApiRequestQueryParams("param1=value1");
        Assert.assertEquals("value1", paramMap.get("param1"));

        paramMap = GatewayServlet.parseApiRequestQueryParams("param1=value1&param2");
        Assert.assertEquals("value1", paramMap.get("param1"));
        Assert.assertNull(paramMap.get("param2"));

        paramMap = GatewayServlet.parseApiRequestQueryParams("param1=value1&param2=value2");
        Assert.assertEquals("value1", paramMap.get("param1"));
        Assert.assertEquals("value2", paramMap.get("param2"));

        paramMap = GatewayServlet.parseApiRequestQueryParams("param1=value1&param2=value2&param3=value3");
        Assert.assertEquals("value1", paramMap.get("param1"));
        Assert.assertEquals("value2", paramMap.get("param2"));
        Assert.assertEquals("value3", paramMap.get("param3"));

        paramMap = GatewayServlet.parseApiRequestQueryParams("param1=hello%20world&param2=hello+world&param3=hello%20world");
        Assert.assertEquals("hello world", paramMap.get("param1"));
        Assert.assertEquals("hello world", paramMap.get("param2"));
        Assert.assertEquals("hello world", paramMap.get("param3"));
    }

}
