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
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.test.policies.ApimanPolicyTest;
import io.apiman.test.policies.BackEndService;
import io.apiman.test.policies.Configuration;
import io.apiman.test.policies.IPolicyTestBackEndService;
import io.apiman.test.policies.PolicyTestBackEndServiceResponse;
import io.apiman.test.policies.PolicyTestRequest;
import io.apiman.test.policies.PolicyTestRequestType;
import io.apiman.test.policies.PolicyTestResponse;
import io.apiman.test.policies.TestingPolicy;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@TestingPolicy(URLRewritingPolicy.class)
@SuppressWarnings("nls")
public class URLRewritingPolicyTest extends ApimanPolicyTest {

    @Test
    @Configuration("{" +
            "  \"fromRegex\" : \"http://localhost:8080/path/to/api\",\n" +
            "  \"toReplacement\" : \"http://example.org:8888/my-api/api-path\",\n" +
            "  \"processBody\" : true,\n" +
            "  \"processHeaders\" : true\n" +
            "}")
    @BackEndService(URLRewritingTestBackend.class)
    public void testFullRewriting() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/path/to/resource");

        PolicyTestResponse response = send(request);
        Assert.assertEquals("http://example.org:8888/my-api/api-path/specific-resource/action", response.headers().get("Location"));
        Assert.assertEquals("application/json", response.headers().get("Content-Type"));
        Assert.assertEquals("no-cache", response.headers().get("Cache"));
        Assert.assertEquals("http://example.org:8888/my-api/api-path/alt-resource/alt-action", response.headers().get("X-Alt-Location"));
        Assert.assertEquals("http://localhost:8080/path-12948792147", response.headers().get("ETag"));
        String responseBody = response.body();
        Assert.assertNotNull(responseBody);
        Assert.assertEquals("\r\n" +
                "{\r\n" +
                "  \"property-1\" : \"value-1\",\r\n" +
                "  \"property-2\" : \"value-2\",\r\n" +
                "  \"property-3\" : \"value-3\",\r\n" +
                "  \"api-location\" : \"http://example.org:8888/my-api/api-path/other-action/here\",\r\n" +
                "  \"property-4\" : \"value-4\",\r\n" +
                "  \"api-alt\" : \"http://example.org:8888/my-api/api-path/alt-action/there\"\r\n" +
                "}", responseBody);
    }

    public static final class URLRewritingTestBackend implements IPolicyTestBackEndService {

        /**
         * @see io.apiman.test.policies.IPolicyTestBackEndService#invoke(io.apiman.gateway.engine.beans.ServiceRequest, byte[])
         */
        @Override
        public PolicyTestBackEndServiceResponse invoke(ServiceRequest request, byte[] requestBody) {
            ServiceResponse sresponse = new ServiceResponse();
            sresponse.setMessage("OK");
            sresponse.setCode(200);
            sresponse.getHeaders().put("Location", "http://localhost:8080/path/to/api/specific-resource/action");
            sresponse.getHeaders().put("Content-Type", "application/json");
            sresponse.getHeaders().put("Cache", "no-cache");
            sresponse.getHeaders().put("X-Alt-Location", "http://localhost:8080/path/to/api/alt-resource/alt-action");
            sresponse.getHeaders().put("ETag", "http://localhost:8080/path-12948792147");

            String body = "\r\n" +
                    "{\r\n" +
                    "  \"property-1\" : \"value-1\",\r\n" +
                    "  \"property-2\" : \"value-2\",\r\n" +
                    "  \"property-3\" : \"value-3\",\r\n" +
                    "  \"api-location\" : \"http://localhost:8080/path/to/api/other-action/here\",\r\n" +
                    "  \"property-4\" : \"value-4\",\r\n" +
                    "  \"api-alt\" : \"http://localhost:8080/path/to/api/alt-action/there\"\r\n" +
                    "}";

            return new PolicyTestBackEndServiceResponse(sresponse, body);
        }

    }
}
