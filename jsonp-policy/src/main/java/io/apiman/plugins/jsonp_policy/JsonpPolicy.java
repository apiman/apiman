package io.apiman.plugins.jsonp_policy;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.ByteBuffer;
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

    private static final String OPEN_PARENTHESES = "(";
    private static final String CLOSE_PARENTHESES = ")";
	static final String CALLBACK_FUNCTION_NAME = "callbackFunctionName";

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
            IPolicyContext context) {
        final String callbackFunctionName = (String) context.getAttribute(CALLBACK_FUNCTION_NAME, null);
        
        if (callbackFunctionName != null) {
        	response.getHeaders().put("Content-Type", "application/javascript");
        	
            return new AbstractStream<ServiceResponse>() {

                @Override
                public ServiceResponse getHead() {
                    return response;
                }

                @Override
                protected void handleHead(ServiceResponse head) {
                }
                
                @Override
                public void write(IApimanBuffer chunk) {
                	StringBuilder response = new StringBuilder();
                	response.append(callbackFunctionName);
                	response.append(OPEN_PARENTHESES);
                	response.append(chunk.toString());
                	response.append(CLOSE_PARENTHESES);
                	byte[] bytes = response.toString().getBytes();

                    IApimanBuffer newChunk = new ByteBuffer(bytes.length);
                    newChunk.append(bytes);
                    
                    super.write(newChunk);
                }
                
            };
        }
        return null;
    }
}
