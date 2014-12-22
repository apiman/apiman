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

import io.apiman.gateway.platforms.servlet.GatewayServlet.ServiceRequestPathInfo;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
public class GatewayServletTest {

    /**
     * Test method for {@link io.apiman.gateway.platforms.servlet.GatewayServlet#parseServiceRequestPath(java.lang.String)}.
     */
    @SuppressWarnings("nls")
    @Test
    public void testParseServiceRequestPath() {
        ServiceRequestPathInfo info = GatewayServlet.parseServiceRequestPath(null);
        
        info = GatewayServlet.parseServiceRequestPath("/invalidpath");
        Assert.assertNull(info.orgId);
        Assert.assertNull(info.serviceId);
        Assert.assertNull(info.serviceVersion);
        Assert.assertNull(info.resource);
        
        info = GatewayServlet.parseServiceRequestPath("/invalid/path");
        Assert.assertNull(info.orgId);
        Assert.assertNull(info.serviceId);
        Assert.assertNull(info.serviceVersion);
        Assert.assertNull(info.resource);
        
        info = GatewayServlet.parseServiceRequestPath("/Org1/Service1/1.0");
        Assert.assertEquals("Org1", info.orgId);
        Assert.assertEquals("Service1", info.serviceId);
        Assert.assertEquals("1.0", info.serviceVersion);
        Assert.assertNull(info.resource);
        
        info = GatewayServlet.parseServiceRequestPath("/MyOrg/Service-99/2.7");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Service-99", info.serviceId);
        Assert.assertEquals("2.7", info.serviceVersion);
        Assert.assertNull(info.resource);

        info = GatewayServlet.parseServiceRequestPath("/MyOrg/Service-99/2.7/resource");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Service-99", info.serviceId);
        Assert.assertEquals("2.7", info.serviceVersion);
        Assert.assertEquals("/resource", info.resource);

        info = GatewayServlet.parseServiceRequestPath("/MyOrg/Service-99/2.7/path/to/resource");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Service-99", info.serviceId);
        Assert.assertEquals("2.7", info.serviceVersion);
        Assert.assertEquals("/path/to/resource", info.resource);

        info = GatewayServlet.parseServiceRequestPath("/MyOrg/Service-99/2.7/path/to/resource?query=1234");
        Assert.assertEquals("MyOrg", info.orgId);
        Assert.assertEquals("Service-99", info.serviceId);
        Assert.assertEquals("2.7", info.serviceVersion);
        Assert.assertEquals("/path/to/resource?query=1234", info.resource);
    }

}
