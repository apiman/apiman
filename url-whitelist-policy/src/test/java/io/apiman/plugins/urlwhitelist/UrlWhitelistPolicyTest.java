package io.apiman.plugins.urlwhitelist;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.test.policies.*;
import org.junit.Test;

import java.net.HttpURLConnection;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("nls")
@TestingPolicy(UrlWhitelistPolicy.class)
public class UrlWhitelistPolicyTest extends ApimanPolicyTest {

    @Test
    @Configuration(classpathConfigFile = "basic-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testPermitted() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/PolicyTester/TestApi/1/allow/example");
        final PolicyTestResponse response = send(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.code());
        assertNotNull(response.body());
    }

    @Test
    @Configuration(classpathConfigFile = "basic-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testDenied() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/PolicyTester/TestApi/1/deny/example");

        try {
            send(request);
            fail(PolicyFailureError.class + " expected");

        } catch (PolicyFailureError policyFailureError) {
            final PolicyFailure failure = policyFailureError.getFailure();

            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, failure.getFailureCode());
            assertEquals(PolicyFailureType.Authorization, failure.getType());
        }
    }
}
