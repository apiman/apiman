package io.apiman.plugins.uniqueheader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;
import io.apiman.test.policies.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Policy tests for {@link UniqueHeaderPolicy} plugin.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
@TestingPolicy(UniqueHeaderPolicy.class)
public class UniqueHeaderPolicyTest extends ApimanPolicyTest {
    private static ObjectMapper jsonMapper;

    /**
     * Shared test initialisation.
     */
    @BeforeClass
    public static void setUp() {
        jsonMapper = new ObjectMapper();
    }

    /**
     * Expects that a unique value is set with the HTTP Header name 'X-CorrelationID'.
     *
     * @throws Throwable
     */
    @SuppressWarnings("unchecked")
    @Test
    @Configuration(classpathConfigFile = "basic-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testUniqueValueSet() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/example");
        final PolicyTestResponse response = send(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.code());
        assertNotNull(response.body());

        // the headers are mirrored back by the EchoBackEndApi in its response body
        final Map<String, Object> responseAsMap = jsonMapper.readValue(response.body(), HashMap.class);
        final Map<String, Object> headers = (Map<String, Object>) responseAsMap.get("headers");
        assertNotNull(headers);
        assertEquals(1, headers.size());
        assertNotNull(headers.get("X-CorrelationID"));
    }

    /**
     * Expects that a {@link ConfigurationParseException} is thrown if the header name configuration item is not set.
     *
     * @throws Throwable
     */
    @Test(expected = ConfigurationParseException.class)
    @Configuration(classpathConfigFile = "empty-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testValidateConfiguration() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/example");
        send(request);

        fail(ConfigurationParseException.class + " expected");
    }
}
