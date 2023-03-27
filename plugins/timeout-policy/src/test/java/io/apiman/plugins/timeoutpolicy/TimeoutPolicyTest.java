package io.apiman.plugins.timeoutpolicy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.plugins.timeoutpolicy.beans.TimeoutConfigBean;
import io.apiman.test.policies.ApimanPolicyTest;
import io.apiman.test.policies.BackEndApi;
import io.apiman.test.policies.Configuration;
import io.apiman.test.policies.IPolicyTestBackEndApi;
import io.apiman.test.policies.PolicyFailureError;
import io.apiman.test.policies.PolicyTestBackEndApiResponse;
import io.apiman.test.policies.PolicyTestRequest;
import io.apiman.test.policies.PolicyTestRequestType;
import io.apiman.test.policies.PolicyTestResponse;
import io.apiman.test.policies.TestingPolicy;

/**
 * @author William Beck {@literal <william.beck.pro@gmail.com>}
 */
@TestingPolicy(TimeoutPolicy.class)
public class TimeoutPolicyTest extends ApimanPolicyTest {
    private TimeoutPolicy timeoutPolicy = new TimeoutPolicy();

    /**
     * Control the type of the config bean
     */
    @Test
    @Configuration("{\"timeoutConnect\" : \"1\", \"timeoutRead\" : \"1\" }")
    public void shouldReturnTimeoutConfigBean_onGetConfigurationClass() {
        // WHEN retrieving the configuration class
        Class<?> confClass = timeoutPolicy.getConfigurationClass();

        // THEN it must be the payload bean config
        assertEquals(TimeoutConfigBean.class, confClass);
    }

    /**
     * Control the parse system for the configuration
     */
    @Test
    @Configuration("{\"timeoutConnect\" : \"1\", \"timeoutRead\" : \"2\" }")
    public void shouldReturnConfigBeanWithValue_onParseConfiguration() throws PolicyFailureError, Throwable {
        Configuration config = this.getClass()
                .getMethod("shouldReturnConfigBeanWithValue_onParseConfiguration")
                .getAnnotation(Configuration.class);

        // WHEN parse the configuration
        TimeoutConfigBean policyConfigBean = timeoutPolicy.parseConfiguration(config.value());

        // THEN the bean's value equals the test config
        HashMap<?, ?> configMap = new ObjectMapper().readValue(config.value(), HashMap.class);
        assertEquals(configMap.get("timeoutConnect"), policyConfigBean.getTimeoutConnect());
        assertEquals(configMap.get("timeoutRead"), policyConfigBean.getTimeoutRead());
    }

    /**
     * Control the normal execution
     */
    @Test
    @Configuration("{\"timeoutConnect\" : \"1\", \"timeoutRead\" : \"2\" }")
    @BackEndApi(EndPointPropertiesEcho.class)
    public void shouldExecute_onSimpleConfiguration() throws PolicyFailureError, Throwable {
        // WHEN Execute the policy
        PolicyTestResponse response = null;
        try {
            response = send(PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource"));
        } catch (PolicyFailureError ex) {
            fail("Configuration error");
        }
        // THEN timeouts are set
        HashMap<?, ?> responseMap = new ObjectMapper().readValue(response.body(), HashMap.class);
        assertEquals("timeoutConnect", "1", responseMap.get("timeouts.connect"));
        assertEquals("timeoutRead", "2", responseMap.get("timeouts.read"));
    }

    /**
     * Execute with no value
     */
    @Test
    @Configuration("{}")
    @BackEndApi(EndPointPropertiesEcho.class)
    public void shouldsettingNothing_onNoValue() throws PolicyFailureError, Throwable {
        // WHEN Execute the policy
        PolicyTestResponse response = null;
        try {
            response = send(PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource"));
        } catch (PolicyFailureError ex) {
            fail("Configuration error");
        }
        // THEN timeouts are not set
        HashMap<?, ?> responseMap = new ObjectMapper().readValue(response.body(), HashMap.class);
        assertFalse("timeoutConnect", responseMap.containsKey("timeouts.connect"));
        assertFalse("timeoutRead", responseMap.containsKey("timeouts.read"));
    }

    /**
     * Execute with empty value
     */
    @Test
    @Configuration("{\"timeoutConnect\" : \"\", \"timeoutRead\" : \"\" }")
    @BackEndApi(EndPointPropertiesEcho.class)
    public void shouldsettingNothing_onEmptyValue() throws PolicyFailureError, Throwable {
        // WHEN Execute the policy
        PolicyTestResponse response = null;
        try {
            response = send(PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource"));
        } catch (PolicyFailureError ex) {
            fail("Configuration error");
        }
        // THEN timeouts are not set
        HashMap<?, ?> responseMap = new ObjectMapper().readValue(response.body(), HashMap.class);
        assertFalse("timeoutConnect", responseMap.containsKey("timeouts.connect"));
        assertFalse("timeoutRead", responseMap.containsKey("timeouts.read"));
    }

    /**
     * 
     * Return the EndPointProperties in response body
     *
     */
    public static final class EndPointPropertiesEcho implements IPolicyTestBackEndApi {
        @Override
        public PolicyTestBackEndApiResponse invoke(ApiRequest request, byte[] requestBody) {
            ApiResponse response = new ApiResponse();
            response.setMessage("OK");
            response.setCode(200);
            String responseBody = "{}";
            try {
                responseBody = new ObjectMapper()
                        .writeValueAsString(request.getApi().getEndpointProperties());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return new PolicyTestBackEndApiResponse(response, responseBody);
        }
    }
}
