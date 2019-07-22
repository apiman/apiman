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

import io.apiman.test.common.mock.EchoResponse;
import io.apiman.test.policies.*;
import io.apiman.gateway.engine.beans.Api;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test.
 *
 * @author benjamin.kihm@scheer-group.com
 */
@TestingPolicy(CachingResourcesPolicy.class)
@SuppressWarnings("nls")
public class CachingResourcesPolicyTest extends ApimanPolicyTest {
    /**
     * Basic test for caching a request and time to live
     * @throws Throwable
     */
    @Test
    @Configuration("{" +
            "  \"ttl\" : 2," +
            "  \"cachingResourcesSettingsEntries\" : [{\"statusCode\": \"*\", \"pathPattern\": \"*\", \"httpMethod\": \"*\"}]" +
            "}")
    public void testCaching() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/cached-resource");
        assertRequestIsCached(request);

        Long counterValue = doRequest(request);
        // Now wait for 3s and make sure the cache entry expired
        Thread.sleep(3000);

        Long counterValue2 = doRequest(request);
        //result should not cached
        assertNotEquals(counterValue, counterValue2);

        // And again - should be re-cached
        Long counterValue3 = doRequest(request);
        assertEquals(counterValue2, counterValue3);
    }

    /**
     * Verify that the query string is used as part of the cache key - expect
     * that requests with different query strings are treated as different, from
     * a caching perspective.
     */
    @Test
    @Configuration("{" +
            "  \"ttl\" : 2," +
            "  \"cachingResourcesSettingsEntries\" : [{\"statusCode\": \"*\", \"pathPattern\": \"*\", \"httpMethod\": \"*\"}]" +
            "}")
    public void testCachingUsingQueryString() throws Throwable {
        final String originalUri = "/some/cached-resource?foo=bar";
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, originalUri);
        // Request the original URI (including query string) - expect a cached response
        assertRequestIsCached(request);

        // Request with a different query string - expect an uncached response
        PolicyTestRequest differentQueryStringRequest = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/cached-resource?foo=different");
        Long differentQueryStringRequestResponse = doRequest(differentQueryStringRequest);
        Long originalRequestResponse = doRequest(request);
        assertNotEquals(originalRequestResponse, differentQueryStringRequestResponse);
    }


    /**
     * Verify that caching works with pathpattern containing a wildcard in the middle of a path
     * @throws Throwable
     */
    @Test
    @Configuration("{" +
            "  \"ttl\" : 5," +
            "  \"cachingResourcesSettingsEntries\" : [{\"statusCode\": \"*\", \"pathPattern\": \"/foo/.+/bar\", \"httpMethod\": \"*\"}]" +
            "}")
    public void testCachingPathPatternWildcardInfix() throws Throwable {
        //Do first request which should be cached
        final String originalUri = "/foo/test/bar";
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, originalUri);
        assertRequestIsCached(request);

        //Do request which should not be cached by pathPattern
        final String notCachedOriginalUri = "/bar/test/foo";
        PolicyTestRequest requestNoCache = PolicyTestRequest.build(PolicyTestRequestType.GET, notCachedOriginalUri);
        assertRequestNotCached(requestNoCache);
    }

    /**
     * Verify that caching works with pathpattern containing a wildcard at the path end
     * @throws Throwable
     */
    @Test
    @Configuration("{" +
            "  \"ttl\" : 5," +
            "  \"cachingResourcesSettingsEntries\" : [{\"statusCode\": \"*\", \"pathPattern\": \"/foo/.*\", \"httpMethod\": \"*\"}]" +
            "}")
    public void testCachingPathPatternWildcardPostfix() throws Throwable {
        final String originalUri = "/foo/test/bar";
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, originalUri);
        assertRequestIsCached(request);
    }

    /**
     * Ensures that cachable http methods are cached. (See  https://developer.mozilla.org/en-US/docs/Glossary/cacheable)
     * @throws Throwable
     */
    @Test
    @Configuration("{" +
            "  \"ttl\" : 5," +
            "  \"cachingResourcesSettingsEntries\" : [{\"statusCode\": \"*\", \"pathPattern\": \".*\", \"httpMethod\": \"*\"}]" +
            "}")
    public void testCachableHttpMethods() throws Throwable {
        PolicyTestRequestType[] cachableHttpMethods = {
                PolicyTestRequestType.GET,
                PolicyTestRequestType.POST,
                PolicyTestRequestType.HEAD
        };

        for (PolicyTestRequestType cachableHttpMethod : cachableHttpMethods) {
            final String originalUri = "/cachable";
            PolicyTestRequest request = PolicyTestRequest.build(cachableHttpMethod, originalUri);
            assertRequestIsCached(request);
        }
    }

    /**
     * Ensures that non cachable http methods are not cached. (See  https://developer.mozilla.org/en-US/docs/Glossary/cacheable)
     * @throws Throwable
     */
    @Test
    @Configuration("{" +
            "  \"ttl\" : 5," +
            "  \"cachingResourcesSettingsEntries\" : [{\"statusCode\": \"*\", \"pathPattern\": \".*\", \"httpMethod\": \"*\"}]" +
            "}")
    public void testNonCachableHttpMethods() throws Throwable {
        PolicyTestRequestType[] nonCachableHttpMethods = {
                PolicyTestRequestType.PUT,
                PolicyTestRequestType.DELETE,
                PolicyTestRequestType.OPTIONS,
                PolicyTestRequestType.TRACE,
                PolicyTestRequestType.CONNECT,
                PolicyTestRequestType.PATCH
        };

        for (PolicyTestRequestType nonCachableHttpMethod : nonCachableHttpMethods) {
            final String originalUri = "/notCachable";
            PolicyTestRequest request = PolicyTestRequest.build(nonCachableHttpMethod, originalUri);
            assertRequestNotCached(request);
        }
    }

    /**
     * Verify that requests with specific status code or http method are cached and others not.
     * @throws Throwable
     */
    @Test
    @Configuration("{" +
            "  \"ttl\" : 5," +
            "  \"cachingResourcesSettingsEntries\" : [{\"statusCode\": \"200\", \"pathPattern\": \".*\", \"httpMethod\": \"GET\"}]" +
            "}")
    public void testCachingWithStatusCodeAndHttpMethod() throws Throwable {
        final String originalUri = "/foo/test/bar";
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, originalUri);
        //Do first request which should be cached
        Long firstRequestResult = doRequest(request);
        // Now send the request again - we should get the *same* counter value!
        Long secondRequestResult = doRequest(request);
        assertEquals(firstRequestResult, secondRequestResult);

        final String notFoundUri = "/not/found";
        PolicyTestRequest requestNoCache = PolicyTestRequest.build(PolicyTestRequestType.GET, notFoundUri);
        requestNoCache.headers().put("X-Echo-ErrorCode", "404");
        requestNoCache.headers().put("X-Echo-ErrorMessage", "Not found.");
        Long noCachedRequestResult = doRequest(requestNoCache);
        assertTrue(noCachedRequestResult > secondRequestResult);
        //send again /not/found request
        assertRequestNotCached(requestNoCache);

        //Do post request which should not be cached
        PolicyTestRequest requestPost = PolicyTestRequest.build(PolicyTestRequestType.POST, originalUri);
        assertRequestNotCached(requestPost);
    }

    @Test
    @Configuration("{" +
            "  \"ttl\" : 5," +
            "  \"cachingResourcesSettingsEntries\" : [{\"statusCode\": \"42\", \"pathPattern\": \".*\", \"httpMethod\": \"*\"}, {\"statusCode\": \"300\", \"pathPattern\": \".*\", \"httpMethod\": \"*\"}]" +
            "}")
    public void testMultipleCachingResourcesSettingsEntries() throws Throwable {
        PolicyTestRequest requestCache = PolicyTestRequest.build(PolicyTestRequestType.GET, "/testStatusCode");
        requestCache.headers().put("X-Echo-ErrorCode", "42");
        requestCache.headers().put("X-Echo-ErrorMessage", "Should cached.");
        assertRequestIsCached(requestCache);

        PolicyTestRequest requestCache2 = PolicyTestRequest.build(PolicyTestRequestType.GET, "/testStatusCode/different");
        requestCache2.headers().put("X-Echo-ErrorCode", "300");
        requestCache2.headers().put("X-Echo-ErrorMessage", "Should cached.");
        assertRequestIsCached(requestCache2);
    }

    @Test
    @Configuration("{" +
            "  \"ttl\" : 5," +
            "  \"cachingResourcesSettingsEntries\" : [{\"statusCode\": \"*\", \"pathPattern\": \".*\", \"httpMethod\": \"*\"}]" +
            "}")
    public void testPostRequestCaching() throws Throwable {
        PolicyTestRequest requestCache = PolicyTestRequest.build(PolicyTestRequestType.POST, "/testPostCache");
        requestCache.body("Test");
        Long counterValue = doRequest(requestCache);

        requestCache.body("NotCached");
        Long shouldNotCachedcounterValue = doRequest(requestCache);
        assertNotEquals(counterValue, shouldNotCachedcounterValue);
    }

    /**
     * Ensures that a request is cached.
     * @param request
     * @throws Throwable
     */
    private void assertRequestIsCached(PolicyTestRequest request) throws Throwable {
        //Do first request which should be cached
        Long counterValue = doRequest(request);
        // Now send the request again - we should get the *same* counter value!
        Long counterValue2 = doRequest(request);
        assertEquals(counterValue, counterValue2);
    }

    /**
     * Ensures that a request is not cached.
     * @param request
     * @throws Throwable
     */
    private void assertRequestNotCached(PolicyTestRequest request) throws Throwable {
        //Do first request which should not be cached
        Long counterValue = doRequest(request);
        // Now send the request again - we should get the *same* counter value!
        Long counterValue2 = doRequest(request);
        assertNotEquals(counterValue, counterValue2);
    }

    /**
     * Simulate a request
     * @param request
     * @return counterValue as result of the request
     * @throws Throwable
     */
    private Long doRequest(PolicyTestRequest request) throws Throwable {
        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        assertNotNull(echo);
        Long counterValue = echo.getCounter();
        assertNotNull(counterValue);
        assertEquals("application/json", response.header("Content-Type"));
        return counterValue;
    }
}
