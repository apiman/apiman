/*
 * 2012-3 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.apiman.gateway.undertow;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.overlord.apiman.Request;
import org.overlord.apiman.Response;
import org.overlord.apiman.gateway.Gateway;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import javax.inject.Inject;

/**
 * This class implements the Undertow HTTP handler for the gateway.
 * 
 */
public class UndertowGateway implements HttpHandler {

	@Inject
	private Gateway _gateway=null;
	
	/**
	 * This method returns the gateway.
	 * 
	 * @return The gateway
	 */
	public Gateway getGateway() {
	    return (_gateway);
	}
	
    /**
     * This method sets the gateway.
     * 
     * @param gw The gateway
     */
	public void setGateway(Gateway gw) {
	    _gateway = gw;
	}
	
	@Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
	    
		if (_gateway == null) {
			throw new IOException("No gateway available");
		}
		
		try {
			// Forward the request to the gateway
			Request request=new UndertowGatewayRequest(exchange);			
			
			Response resp=_gateway.process(request);
			
            exchange.setResponseCode(resp.getStatusCode());
			exchange.getResponseSender().send(ByteBuffer.wrap(resp.getContent()));
			
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception e) {
			throw new IOException("Failed to process gateway request", e);
		}
	}
}
