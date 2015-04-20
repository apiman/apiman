package io.apiman.plugins.jsonp_policy;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.io.ByteBuffer;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.policy.PolicyContextImpl;
import io.apiman.plugins.jsonp_policy.beans.JsonpConfigBean;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class JsonpPolicyTest {

    private JsonpPolicy jsonpPolicy;
    
    @Before
    public void setUp() {
        jsonpPolicy = new JsonpPolicy();
    }
    
    @Test
    public void testParseEmptyConfiguration() {
        // given
        String config = "{}";
        // when
        JsonpConfigBean jsonpConfig = jsonpPolicy.parseConfiguration(config);
        // then
        assertNull(jsonpConfig.getCallbackParamName());
    }

    @Test
    public void testParseRealConfiguration() {
        // given
        String parameterName = "jsonp";
        String config = "{\"callbackParamName\":\"" + parameterName + "\"}";
        // when
        JsonpConfigBean jsonpConfig = jsonpPolicy.parseConfiguration(config);
        // then
        assertEquals(parameterName, jsonpConfig.getCallbackParamName());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldSaveCallbackParamNameInContext() throws Exception {
        // given
        JsonpConfigBean config = new JsonpConfigBean();
        config.setCallbackParamName("testParam");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("testParam", "testFunction");
        ServiceRequest request = new ServiceRequest();
        request.setQueryParams(queryParams);
        IPolicyContext context = new PolicyContextImpl(null);
        IPolicyChain<ServiceRequest> chain = mock(IPolicyChain.class);
        // when
        jsonpPolicy.doApply(request, context, config, chain);
        // then
        assertEquals("testFunction", context.getAttribute("callbackFunctionName", null));
        verify(chain).doApply(request);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRemoveCallbackParamNameFromRequest() throws Exception {
        // given
        JsonpConfigBean config = new JsonpConfigBean();
        config.setCallbackParamName("testParam");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("testParam", "testFunction");
        ServiceRequest request = new ServiceRequest();
        request.setQueryParams(queryParams);
        IPolicyContext context = new PolicyContextImpl(null);
        IPolicyChain<ServiceRequest> chain = mock(IPolicyChain.class);
        // when
        jsonpPolicy.doApply(request, context, config, chain);
        // then
        assertNull(request.getQueryParams().get("testParam"));
        verify(chain).doApply(request);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void changeResponseWhenCallbackParamNameIsSavedInContext() throws Exception {
        // given
        IPolicyContext context = new PolicyContextImpl(null);
        String functionName = "testFunction";
		context.setAttribute(JsonpPolicy.CALLBACK_FUNCTION_NAME, functionName);
        ServiceResponse response = new ServiceResponse();
        String json = "{\"name\": \"test\"}";
        IApimanBuffer chunk = new ByteBuffer(json.getBytes().length);
        chunk.append(json);
        IAsyncHandler<IApimanBuffer> bodyHandler = mock(IAsyncHandler.class);
        // when
        IReadWriteStream<ServiceResponse> responseDataHandler = jsonpPolicy.getResponseDataHandler(response, context);
        ServiceResponse head = responseDataHandler.getHead();
        responseDataHandler.bodyHandler(bodyHandler);
        responseDataHandler.write(chunk);
        // then
        assertSame(response, head);
        String javascript = "testFunction(" + json + ")";
        int newChunkSize = javascript.getBytes().length;
        IApimanBuffer newChunk = new ByteBuffer(newChunkSize);
        newChunk.append(javascript);
        verify(bodyHandler).handle(refEq(newChunk));
    }

    @Test
    public void doNotChangeResponseWhenCallbackParamNameIsNotSavedInContext() throws Exception {
        // given
        IPolicyContext context = new PolicyContextImpl(null);
        ServiceResponse response = new ServiceResponse();
        // when
        IReadWriteStream<ServiceResponse> responseDataHandler = jsonpPolicy.getResponseDataHandler(response, context);
        // then
        assertNull(responseDataHandler);
    }
    
}
