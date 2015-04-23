package io.apiman.plugins.jsonp_policy;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IDataPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.jsonp_policy.beans.JsonpConfigBean;

/**
 * Policy that turns an endpoint into a JSONP compatible endpoint. It removes the callback
 * function param from the request and uses it to wrap the response. The content type is set
 * to <code>application/javascript</code>.
 *
 * @see <a href="http://en.wikipedia.org/wiki/JSONP" target="_blank">JSONP - Wikipedia</a>
 *
 * @author Alexandre Kieling <alex.kieling@gmail.com>
 */
public class JsonpPolicy extends AbstractMappedPolicy<JsonpConfigBean> implements IDataPolicy {

	private static final String OPEN_PARENTHESES = "("; //$NON-NLS-1$
    private static final String CLOSE_PARENTHESES = ")"; //$NON-NLS-1$
	static final String CALLBACK_FUNCTION_NAME = "callbackFunctionName"; //$NON-NLS-1$
	private static final String APPLICATION_JAVASCRIPT = "application/javascript"; //$NON-NLS-1$
	private static final String CONTENT_TYPE = "Content-Type"; //$NON-NLS-1$

    @Override
    protected Class<JsonpConfigBean> getConfigurationClass() {
        return JsonpConfigBean.class;
    }

    @Override
    protected void doApply(ServiceRequest request, IPolicyContext context, JsonpConfigBean config,
            IPolicyChain<ServiceRequest> chain) {
        String callbackParamName = config.getCallbackParamName();
        if (callbackParamName != null) {
        	String callbackFunctionName = request.getQueryParams().remove(callbackParamName);
        	if (callbackFunctionName != null) {
        		context.setAttribute(CALLBACK_FUNCTION_NAME, callbackFunctionName);
        	}
        }
        super.doApply(request, context, config, chain);
    }

    @Override
    public IReadWriteStream<ServiceRequest> getRequestDataHandler(ServiceRequest request, IPolicyContext context) {
        return null;
    }

    @Override
    public IReadWriteStream<ServiceResponse> getResponseDataHandler(final ServiceResponse response,
            final IPolicyContext context) {
        final String callbackFunctionName = (String) context.getAttribute(CALLBACK_FUNCTION_NAME, null);

        if (callbackFunctionName != null) {
        	//response.getHeaders()
        	response.getHeaders().put(CONTENT_TYPE, APPLICATION_JAVASCRIPT); //$NON-NLS-1$ //$NON-NLS-2$
        	
        	final IBufferFactoryComponent bufferFactory = context.getComponent(IBufferFactoryComponent.class);

            return new AbstractStream<ServiceResponse>() {
                private boolean firstChunk = true;

                @Override
                public ServiceResponse getHead() {
                    return response;
                }

                @Override
                protected void handleHead(ServiceResponse head) {
                }

                @Override
                public void write(IApimanBuffer chunk) {
                    if (firstChunk) {
                        IApimanBuffer buffer = bufferFactory.
                                createBuffer(callbackFunctionName.length() + OPEN_PARENTHESES.length());
                        buffer.append(callbackFunctionName);
                        buffer.append(OPEN_PARENTHESES);
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
                        IApimanBuffer buffer = bufferFactory.createBuffer(CLOSE_PARENTHESES);
                        super.write(buffer);
                    }
                    super.end();
                }
            };
        }
        return null;
    }
}
