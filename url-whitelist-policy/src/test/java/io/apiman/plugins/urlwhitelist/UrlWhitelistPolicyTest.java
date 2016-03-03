package io.apiman.plugins.urlwhitelist;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.test.policies.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Policy tests for {@link UrlWhitelistPolicy} plugin.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
@TestingPolicy(UrlWhitelistPolicy.class)
public class UrlWhitelistPolicyTest extends ApimanPolicyTest {
    private static final String API_BASE_URL = "/PolicyTester/TestApi/1";
    private static ObjectMapper jsonMapper;

    /**
     * Shared test initialisation.
     */
    @BeforeClass
    public static void setUp() {
        jsonMapper = new ObjectMapper();
    }

    /**
     * Makes a request with the given {@code method} to the specified {@code resource}, expecting an
     * HTTP 401 Unauthorized response.
     *
     * @param method   the HTTP method
     * @param resource the resource to request
     * @throws Throwable
     */
    private void requestExpectPolicyFailure(PolicyTestRequestType method, String resource) throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(method, resource);

        try {
            send(request);
            fail(PolicyFailureError.class + " expected");

        } catch (PolicyFailureError policyFailureError) {
            final PolicyFailure failure = policyFailureError.getFailure();

            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, failure.getFailureCode());
            assertEquals(PolicyFailureType.Authorization, failure.getType());
        }
    }

    /**
     * Makes a request with the given {@code method} to the specified {@code resource}, expecting an
     * HTTP 200 OK response from the {@link EchoBackEndApi}.
     *
     * @param method   the HTTP method
     * @param resource the resource to request
     * @throws Throwable
     */
    private void requestExpectPolicySuccess(PolicyTestRequestType method, String resource) throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(method, resource);
        final PolicyTestResponse response = send(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.code());
        assertNotNull(response.body());

        // the requested URL is mirrored back by the EchoBackEndApi in its response body
        final Map responseAsMap = jsonMapper.readValue(response.body(), HashMap.class);
        assertEquals(resource, responseAsMap.get("resource"));
    }

    /**
     * Expects that a request meeting both URL and HTTP method rules is permitted to continue to the back-end service.
     *
     * @throws Throwable
     */
    @Test
    @Configuration(classpathConfigFile = "basic-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testAllowedUrlAndMethod() throws Throwable {
        requestExpectPolicySuccess(PolicyTestRequestType.GET, API_BASE_URL + "/allow/example");
        requestExpectPolicySuccess(PolicyTestRequestType.POST, API_BASE_URL + "/allow/example");
        requestExpectPolicySuccess(PolicyTestRequestType.PUT, API_BASE_URL + "/allow/example");
        requestExpectPolicySuccess(PolicyTestRequestType.DELETE, API_BASE_URL + "/allow/example");
        requestExpectPolicySuccess(PolicyTestRequestType.HEAD, API_BASE_URL + "/allow/example");
        requestExpectPolicySuccess(PolicyTestRequestType.OPTIONS, API_BASE_URL + "/allow/example");
        requestExpectPolicySuccess(PolicyTestRequestType.TRACE, API_BASE_URL + "/allow/example");
    }

    /**
     * Expects that a request whose URL is normalised to match an allowed URL rule is permitted
     * to continue to the back-end service.
     *
     * @throws Throwable
     */
    @Test
    @Configuration(classpathConfigFile = "basic-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testUrlNormalisationAllowed() throws Throwable {
        requestExpectPolicySuccess(PolicyTestRequestType.GET, API_BASE_URL + "/../../TestApi/1/allow/example");
    }

    /**
     * Expects that a request whose URL is normalised to match an disallowed URL rule is not permitted
     * to continue to the back-end service.
     *
     * @throws Throwable
     */
    @Test
    @Configuration(classpathConfigFile = "basic-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testUrlNormalisationDenied() throws Throwable {
        requestExpectPolicyFailure(PolicyTestRequestType.GET, API_BASE_URL + "/../../TestApi/1/deny/example");
    }

    /**
     * Expects that a request not meeting the URL rules is not permitted to continue to the back-end service.
     *
     * @throws Throwable
     */
    @Test
    @Configuration(classpathConfigFile = "basic-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testDeniedUrl() throws Throwable {
        requestExpectPolicyFailure(PolicyTestRequestType.GET, API_BASE_URL + "/deny/example");
    }

    /**
     * Expects that a request not meeting the HTTP method rules is not permitted to continue to the back-end service.
     *
     * @throws Throwable
     */
    @Test
    @Configuration(classpathConfigFile = "basic-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testDeniedMethod() throws Throwable {
        // methods are disallowed
        requestExpectPolicyFailure(PolicyTestRequestType.GET, API_BASE_URL + "/mixed/example");
        requestExpectPolicyFailure(PolicyTestRequestType.POST, API_BASE_URL + "/mixed/example");
        requestExpectPolicyFailure(PolicyTestRequestType.PUT, API_BASE_URL + "/mixed/example");

        // same URL as above, but method permitted
        requestExpectPolicySuccess(PolicyTestRequestType.DELETE, API_BASE_URL + "/mixed/example");
        requestExpectPolicySuccess(PolicyTestRequestType.HEAD, API_BASE_URL + "/mixed/example");
        requestExpectPolicySuccess(PolicyTestRequestType.OPTIONS, API_BASE_URL + "/mixed/example");
        requestExpectPolicySuccess(PolicyTestRequestType.TRACE, API_BASE_URL + "/mixed/example");
    }

    /**
     * Expects that, by default, if a whitelist entry does not exist for a URL, the request is not permitted
     * to continue to the back-end service.
     *
     * @throws Throwable
     */
    @Test
    @Configuration(classpathConfigFile = "basic-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testDefaultDenyMissingWhitelist() throws Throwable {
        requestExpectPolicyFailure(PolicyTestRequestType.GET, API_BASE_URL + "/unconfigured/example");
        requestExpectPolicyFailure(PolicyTestRequestType.POST, API_BASE_URL + "/unconfigured/example");
        requestExpectPolicyFailure(PolicyTestRequestType.PUT, API_BASE_URL + "/unconfigured/example");
        requestExpectPolicyFailure(PolicyTestRequestType.DELETE, API_BASE_URL + "/unconfigured/example");
        requestExpectPolicyFailure(PolicyTestRequestType.HEAD, API_BASE_URL + "/unconfigured/example");
        requestExpectPolicyFailure(PolicyTestRequestType.OPTIONS, API_BASE_URL + "/unconfigured/example");
        requestExpectPolicyFailure(PolicyTestRequestType.TRACE, API_BASE_URL + "/unconfigured/example");
    }

    /**
     * Expects that, by default, if a whitelist entry exists for a URL, but is missing method configurations,
     * the request is not permitted to continue to the back-end service.
     *
     * @throws Throwable
     */
    @Test
    @Configuration(classpathConfigFile = "basic-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testDefaultDenyPartiallyConfiguredWhitelist() throws Throwable {
        requestExpectPolicyFailure(PolicyTestRequestType.GET, API_BASE_URL + "/partial/example");
        requestExpectPolicyFailure(PolicyTestRequestType.POST, API_BASE_URL + "/partial/example");
        requestExpectPolicyFailure(PolicyTestRequestType.PUT, API_BASE_URL + "/partial/example");
        requestExpectPolicyFailure(PolicyTestRequestType.DELETE, API_BASE_URL + "/partial/example");
        requestExpectPolicyFailure(PolicyTestRequestType.HEAD, API_BASE_URL + "/partial/example");
        requestExpectPolicyFailure(PolicyTestRequestType.OPTIONS, API_BASE_URL + "/partial/example");
        requestExpectPolicyFailure(PolicyTestRequestType.TRACE, API_BASE_URL + "/partial/example");
    }
}
