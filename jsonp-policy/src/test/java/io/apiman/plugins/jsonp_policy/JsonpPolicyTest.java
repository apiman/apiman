package io.apiman.plugins.jsonp_policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

@SuppressWarnings("nls")
public class JsonpPolicyTest {

    private JsonpPolicy jsonpPolicy;

    @Spy
    private IPolicyContext sContext = new PolicyContextImpl(null);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(new TestBufferFactory()).when(sContext).getComponent(IBufferFactoryComponent.class);

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

    @Test
    public void shouldNotSaveCallbackFunctionNameInContextWhenNotPresent() throws Exception {
        // given
        JsonpConfigBean config = new JsonpConfigBean();
        config.setCallbackParamName("testParam");
        Map<String, String> queryParams = new HashMap<>();
        ServiceRequest request = new ServiceRequest();
        request.setQueryParams(queryParams);

        IPolicyChain<ServiceRequest> chain = mock(IPolicyChain.class);
        // when
        jsonpPolicy.doApply(request, sContext, config, chain);
        // then
        assertNull(sContext.getAttribute("callbackFunctionName", null));
        verify(chain).doApply(request);
    }

    @Test
    public void shouldSaveCallbackParamNameInContextWhenPresent() throws Exception {
        // given
        JsonpConfigBean config = new JsonpConfigBean();
        config.setCallbackParamName("testParam");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("testParam", "testFunction");
        ServiceRequest request = new ServiceRequest();
        request.setQueryParams(queryParams);

        IPolicyChain<ServiceRequest> chain = mock(IPolicyChain.class);
        // when
        jsonpPolicy.doApply(request, sContext, config, chain);
        // then
        assertEquals("testFunction", sContext.getAttribute("callbackFunctionName", null));
        verify(chain).doApply(request);
    }

    @Test
    public void shouldRemoveCallbackParamNameFromRequest() throws Exception {
        // given
        JsonpConfigBean config = new JsonpConfigBean();
        config.setCallbackParamName("testParam");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("testParam", "testFunction");
        ServiceRequest request = new ServiceRequest();
        request.setQueryParams(queryParams);

        IPolicyChain<ServiceRequest> chain = mock(IPolicyChain.class);
        // when
        jsonpPolicy.doApply(request, sContext, config, chain);
        // then
        assertNull(request.getQueryParams().get("testParam"));
        verify(chain).doApply(request);
    }

    @Test
    public void changeResponseWhenCallbackParamNameIsSavedInContext() throws Exception {
        JsonpConfigBean config = new JsonpConfigBean();

        // given
        String functionName = "testFunction";
        sContext.setAttribute(JsonpPolicy.CALLBACK_FUNCTION_NAME, functionName);
        ServiceResponse response = new ServiceResponse();
        String json = "{\"name\": \"test\"}";
        IApimanBuffer chunk = new ByteBuffer(json.getBytes().length);
        chunk.append(json);
        IAsyncHandler<IApimanBuffer> bodyHandler = mock(IAsyncHandler.class);
        // when
        IReadWriteStream<ServiceResponse> responseDataHandler = jsonpPolicy.getResponseDataHandler(response, sContext, config);
        ServiceResponse head = responseDataHandler.getHead();
        responseDataHandler.bodyHandler(bodyHandler);
        responseDataHandler.write(chunk);
        responseDataHandler.end();
        // then
        assertSame(response, head);

        verify(bodyHandler).handle(refEq(new ByteBuffer("testFunction(")));
        verify(bodyHandler).handle(refEq(new ByteBuffer(json)));
        verify(bodyHandler).handle(refEq(new ByteBuffer(")")));
    }

    @Test
    public void doNotChangeResponseWhenCallbackParamNameIsNotSavedInContext() throws Exception {
        JsonpConfigBean config = new JsonpConfigBean();

        // given
        ServiceResponse response = new ServiceResponse();
        // when
        IReadWriteStream<ServiceResponse> responseDataHandler = jsonpPolicy.getResponseDataHandler(response, sContext, config);
        // then
        assertNull(responseDataHandler);
    }

    private static final class TestBufferFactory implements IBufferFactoryComponent {

        @Override
        public IApimanBuffer createBuffer() {
            return new ByteBuffer(40);
        }

        @Override
        public IApimanBuffer createBuffer(int size) {
            return new ByteBuffer(size);
        }

        @Override
        public IApimanBuffer createBuffer(String stringData) {
            return new ByteBuffer(stringData);
        }

        @Override
        public IApimanBuffer createBuffer(String stringData, String enc) {
            return new ByteBuffer(stringData, enc);
        }

        @Override
        public IApimanBuffer createBuffer(byte[] byteData) {
            return new ByteBuffer(byteData);
        }

        @Override
        public IApimanBuffer cloneBuffer(IApimanBuffer buffer) {
            return new ByteBuffer(buffer.getBytes());
        }
    }
}
