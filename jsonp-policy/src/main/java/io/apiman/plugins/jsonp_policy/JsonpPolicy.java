package io.apiman.plugins.jsonp_policy;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IDataPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.jsonp_policy.beans.JsonpConfigBean;
import io.apiman.plugins.jsonp_policy.http.HttpHeaders;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Policy that turns an endpoint into a JSONP compatible endpoint. It removes the callback function param from the
 * request and uses it to wrap the response. The content type is set to <code>application/javascript</code>.
 *
 * @see <a href="http://en.wikipedia.org/wiki/JSONP" target="_blank">JSONP - Wikipedia</a>
 *
 * @author Alexandre Kieling {@literal <alex.kieling@gmail.com>}
 */
public class JsonpPolicy extends AbstractMappedPolicy<JsonpConfigBean> implements IDataPolicy {

    private static final String OPEN_PARENTHESES = "("; //$NON-NLS-1$
    private static final String CLOSE_PARENTHESES = ")"; //$NON-NLS-1$
    static final String CALLBACK_FUNCTION_NAME = "callbackFunctionName"; //$NON-NLS-1$
    private static final String APPLICATION_JAVASCRIPT = "application/javascript"; //$NON-NLS-1$

    @Override
    protected Class<JsonpConfigBean> getConfigurationClass() {
        return JsonpConfigBean.class;
    }

    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, JsonpConfigBean config,
            IPolicyChain<ApiRequest> chain) {
        String callbackParamName = config.getCallbackParamName();
        String callbackFunctionName = request.getQueryParams().get(callbackParamName);
        request.getQueryParams().remove(callbackParamName);
        if (callbackFunctionName != null) {
            context.setAttribute(CALLBACK_FUNCTION_NAME, callbackFunctionName);
        }
        super.doApply(request, context, config, chain);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IDataPolicy#getRequestDataHandler(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    public IReadWriteStream<ApiRequest> getRequestDataHandler(ApiRequest request,
            IPolicyContext context, Object policyConfiguration) {
        return null;
    }

    /**
     * @see io.apiman.gateway.engine.policy.IDataPolicy#getResponseDataHandler(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    public IReadWriteStream<ApiResponse> getResponseDataHandler(final ApiResponse response,
            IPolicyContext context, Object policyConfiguration) {
        final String callbackFunctionName = (String) context.getAttribute(CALLBACK_FUNCTION_NAME, null);

        if (callbackFunctionName != null) {
            HttpHeaders httpHeaders = new HttpHeaders(response.getHeaders());
            final String encoding = httpHeaders.getCharsetFromContentType(StandardCharsets.UTF_8.name());
            final int additionalContentLength = callbackFunctionName.length()
                    + OPEN_PARENTHESES.length() + CLOSE_PARENTHESES.length();

            // JSONP responses should have the Content-Type header set to "application/javascript"
            httpHeaders.setContentType(APPLICATION_JAVASCRIPT);

            // the Content-Length will need to be longer
            httpHeaders.incrementContentLength(additionalContentLength);

            final IBufferFactoryComponent bufferFactory = context.getComponent(IBufferFactoryComponent.class);

            return new AbstractStream<ApiResponse>() {
                private boolean firstChunk = true;

                @Override
                public ApiResponse getHead() {
                    return response;
                }

                @Override
                protected void handleHead(ApiResponse head) {
                }

                @Override
                public void write(IApimanBuffer chunk) {
                    if (firstChunk) {
                        IApimanBuffer buffer = bufferFactory.createBuffer(callbackFunctionName.length()
                                + OPEN_PARENTHESES.length());
                        try {
                            buffer.append(callbackFunctionName, encoding);
                            buffer.append(OPEN_PARENTHESES, encoding);
                        } catch (UnsupportedEncodingException e) {
                            // TODO Review the exception handling. A better approach might be throwing an IOException.
                            throw new RuntimeException(e);
                        }
                        // Write callbackFunctionName(
                        super.write(buffer);
                        firstChunk = false;
                    }
                    super.write(chunk);
                }

                @Override
                public void end() {
                    // Write close parenth ) on end if something has been written
                    if (!firstChunk) {
                        IApimanBuffer buffer = bufferFactory.createBuffer(CLOSE_PARENTHESES.length());
                        try {
                            buffer.append(CLOSE_PARENTHESES, encoding);
                        } catch (UnsupportedEncodingException e) {
                            // TODO Review the exception handling. A better approach might be throwing an IOException.
                            throw new RuntimeException(e);
                        }
                        super.write(buffer);
                    }
                    super.end();
                }
            };
        }
        return null;
    }

}
