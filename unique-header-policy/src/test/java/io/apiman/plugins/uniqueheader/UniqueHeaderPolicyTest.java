package io.apiman.plugins.uniqueheader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;
import io.apiman.test.policies.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Policy tests for {@link UniqueHeaderPolicy} plugin.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
@TestingPolicy(UniqueHeaderPolicy.class)
public class UniqueHeaderPolicyTest extends ApimanPolicyTest {
    private static final String X_CORRELATION_ID = "X-CorrelationID";

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
     * Missing overwriteHeaderValue property should keep the provided header value
     *
     * @throws Throwable
     */
    @SuppressWarnings("unchecked")
    @Test
    @Configuration(classpathConfigFile = "basic-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testUniqueValueSet() throws Throwable {
        final String correlationId = UUID.randomUUID().toString();
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/example")
                                                            .header(X_CORRELATION_ID, correlationId);
        final PolicyTestResponse response = send(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.code());
        assertNotNull(response.body());

        // the headers are mirrored back by the EchoBackEndApi in its response body
        final Map<String, Object> responseAsMap = jsonMapper.readValue(response.body(), HashMap.class);
        final Map<String, Object> headers = (Map<String, Object>) responseAsMap.get("headers");
        assertNotNull(headers);
        assertEquals(1, headers.size());
        assertNotNull(headers.get(X_CORRELATION_ID));
        assertEquals(correlationId, headers.get(X_CORRELATION_ID));
    }

    /**
     * Expects that a unique value is set with the HTTP Header name 'X-CorrelationID'.
     * "overwriteHeaderValue": true property should set a new header value
     *
     * @throws Throwable
     */
    @SuppressWarnings("unchecked")
    @Test
    @Configuration(classpathConfigFile = "overwrite-header-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testOverwriteExistentHeaderValue() throws Throwable {
        final String correlationId = UUID.randomUUID().toString();
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/example")
                                                            .header(X_CORRELATION_ID, correlationId);
        final PolicyTestResponse response = send(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.code());
        assertNotNull(response.body());

        // the headers are mirrored back by the EchoBackEndApi in its response body
        final Map<String, Object> responseAsMap = jsonMapper.readValue(response.body(), HashMap.class);
        final Map<String, Object> headers = (Map<String, Object>) responseAsMap.get("headers");
        assertNotNull(headers);
        assertEquals(1, headers.size());
        assertNotNull(headers.get(X_CORRELATION_ID));
        assertNotEquals(correlationId, headers.get(X_CORRELATION_ID));
    }

    /**
     * Expects that a unique value is set with the HTTP Header name 'X-CorrelationID'.
     * Explicit "overwriteHeaderValue": false property should not set a new header value and keep the value provided
     *
     * @throws Throwable
     */
    @SuppressWarnings("unchecked")
    @Test
    @Configuration(classpathConfigFile = "not-overwrite-header-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testNotOverwriteExistentHeaderValue() throws Throwable {
        final String correlationId = UUID.randomUUID().toString();
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/example")
                                                            .header(X_CORRELATION_ID, correlationId);
        final PolicyTestResponse response = send(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.code());
        assertNotNull(response.body());

        // the headers are mirrored back by the EchoBackEndApi in its response body
        final Map<String, Object> responseAsMap = jsonMapper.readValue(response.body(), HashMap.class);
        final Map<String, Object> headers = (Map<String, Object>) responseAsMap.get("headers");
        assertNotNull(headers);
        assertEquals(1, headers.size());
        assertNotNull(headers.get(X_CORRELATION_ID));
        assertEquals(correlationId, headers.get(X_CORRELATION_ID));
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
