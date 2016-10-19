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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.test.policies.*;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
            "  \"processResponseBody\" : true,\n" +
            "  \"processResponseHeaders\" : true,\n" +
            "  \"processRequestHeaders\" : false,\n" +
            "  \"processRequestUrl\" : false\n" +
            "}")
    @BackEndApi(URLRewritingTestBackend.class)
    public void testFullRewriting() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/path/to/resource");

        final PolicyTestResponse response = send(request);
        Assert.assertEquals("http://example.org:8888/my-api/api-path/specific-resource/action", response.headers().get("Location"));
        Assert.assertEquals("application/json", response.headers().get("Content-Type"));
        Assert.assertEquals("no-cache", response.headers().get("Cache"));
        Assert.assertEquals("http://example.org:8888/my-api/api-path/alt-resource/alt-action", response.headers().get("X-Alt-Location"));
        Assert.assertEquals("http://localhost:8080/path-12948792147", response.headers().get("ETag"));
        final String responseBody = response.body();
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

    /**
     * Rewrite the request URL and a URL in the request header.
     */
    @Test
    @Configuration("{" +
            "  \"fromRegex\" : \"\\/my-api/(.*)\",\n" +
            "  \"toReplacement\" : \"/$1?foo=bar\",\n" +
            "  \"processResponseBody\" : false,\n" +
            "  \"processResponseHeaders\" : false,\n" +
            "  \"processRequestHeaders\" : true,\n" +
            "  \"processRequestUrl\" : true\n" +
            "}")
    @BackEndApi(EchoBackEndApi.class)
    public void testRewriteRequest() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/my-api/api-path");
        request.header("X-Custom-Location", "/my-api/another-path");

        final PolicyTestResponse response = send(request);
        final String responseBody = response.body();
        Assert.assertNotNull(responseBody);

        final HashMap responseMap = new ObjectMapper().readValue(responseBody, HashMap.class);
        Assert.assertEquals("/api-path?foo=bar", responseMap.get("resource"));
        Assert.assertEquals("/another-path?foo=bar", ((Map) responseMap.get("headers")).get("X-Custom-Location"));
    }

    public static final class URLRewritingTestBackend implements IPolicyTestBackEndApi {

        /**
         * @see io.apiman.test.policies.IPolicyTestBackEndApi#invoke(io.apiman.gateway.engine.beans.ApiRequest, byte[])
         */
        @Override
        public PolicyTestBackEndApiResponse invoke(ApiRequest request, byte[] requestBody) {
            ApiResponse sresponse = new ApiResponse();
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

            return new PolicyTestBackEndApiResponse(sresponse, body);
        }

    }
}
