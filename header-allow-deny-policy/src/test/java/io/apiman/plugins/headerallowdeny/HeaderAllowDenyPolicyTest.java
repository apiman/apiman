package io.apiman.plugins.headerallowdeny;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.test.policies.*;
import org.junit.Test;

import java.net.HttpURLConnection;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Policy tests for {@link HeaderAllowDenyPolicy} plugin.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
@TestingPolicy(HeaderAllowDenyPolicy.class)
public class HeaderAllowDenyPolicyTest extends ApimanPolicyTest {
    @Test
    @Configuration(classpathConfigFile = "host-header-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testExplicitAllowHostHeader() throws Throwable {
        requestExpectPolicySuccess("Host", "foo.allow.example.com");
        requestExpectPolicyFailure("Host", "somethingelse.example.com");
    }

    @Test
    @Configuration(classpathConfigFile = "host-header-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testExplicitDenyHostHeader() throws Throwable {
        requestExpectPolicyFailure("Host", "foo.deny.example.com");
    }

    @Test
    @Configuration(classpathConfigFile = "host-header-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testDenyIfHeaderMissing() throws Throwable {
        requestExpectPolicyFailure("X-Another-Header", "bar");
    }

    @Test
    @Configuration(classpathConfigFile = "missing-header-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testAllowIfHeaderMissing() throws Throwable {
        requestExpectPolicyFailure("X-Optional-Header", "bar");
        requestExpectPolicySuccess("X-Another-Header", "bar");
    }

    @Test
    @Configuration(classpathConfigFile = "unmatched-rules-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testAllowIfNoRulesMatch() throws Throwable {
        requestExpectPolicySuccess("X-Optional-Rules", "bar");
    }

    @Test
    @Configuration(classpathConfigFile = "empty-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testAllowIfEmptyConfig() throws Throwable {
        requestExpectPolicySuccess("X-Foo", "bar");
    }

    @Test
    @Configuration(classpathConfigFile = "empty-rules-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testAllowIfEmptyRules() throws Throwable {
        requestExpectPolicyFailure("X-Another-Header", "bar");
    }

    /**
     * Makes a request with the given {@code method} to the specified {@code resource}, expecting an
     * HTTP 403 response status from the policy.
     *
     * @param headerName  the HTTP header name
     * @param headerValue the HTTP header value
     */
    private void requestExpectPolicyFailure(String headerName, String headerValue) throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/example/path");
        request.header(headerName, headerValue);
        try {
            send(request);
            fail(PolicyFailureError.class + " expected");

        } catch (PolicyFailureError policyFailureError) {
            final PolicyFailure failure = policyFailureError.getFailure();

            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, failure.getFailureCode());
            assertEquals(PolicyFailureType.Authorization, failure.getType());
        }
    }

    /**
     * Makes a request with the given {@code method} to the specified {@code resource}, expecting an
     * HTTP 200 response status from the {@link EchoBackEndApi}.
     *
     * @param headerName  the HTTP header name
     * @param headerValue the HTTP header value
     */
    private void requestExpectPolicySuccess(String headerName, String headerValue) throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/example/path");
        request.header(headerName, headerValue);
        final PolicyTestResponse response = send(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.code());
        assertNotNull(response.body());
    }
}
