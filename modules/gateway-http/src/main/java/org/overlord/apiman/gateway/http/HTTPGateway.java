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
package org.overlord.apiman.gateway.http;

import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
import org.overlord.apiman.Request;
import org.overlord.apiman.Response;
import org.overlord.apiman.gateway.Gateway;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class implements the HTTP based wrapper for the gateway.
 * 
 * Based on the http servlet proxy implemented by David Smiley:
 * https://github.com/dsmiley/HTTP-Proxy-Servlet
 */
public class HTTPGateway extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
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
	protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
							throws IOException {
	    
		if (_gateway == null) {
			throw new IOException("No gateway available");
		}
		
		try {
			// Forward the request to the gateway
			Request request=new HTTPGatewayRequest(servletRequest);
			
			Response resp=_gateway.process(request);
			
			servletResponse.setStatus(resp.getStatusCode());
			
			copyResponseHeaders(resp, servletResponse);
	
			// Send the content to the client
			copyResponseContent(resp, servletResponse);
			
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception e) {
			throw new IOException("Failed to process gateway request", e);
		}
	}

	/** Copy proxied response headers back to the servlet client. */
	protected void copyResponseHeaders(Response proxyResponse, HttpServletResponse servletResponse) {
		for (int i=0; i < proxyResponse.getHeaders().size(); i++) {
			org.overlord.apiman.NameValuePair nvp=proxyResponse.getHeaders().get(i);
			
			if (hopByHopHeaders.containsHeader(nvp.getName())) {
				continue;
			}
			
			servletResponse.addHeader(nvp.getName(), (String)nvp.getValue());
		}
	}

	/** Copy response body data (the entity) from the proxy to the servlet client. */
	protected void copyResponseContent(Response proxyResponse, HttpServletResponse servletResponse) throws IOException {
		OutputStream servletOutputStream = servletResponse.getOutputStream();
		
		try {
			servletOutputStream.write(proxyResponse.getContent());
			
		} finally {
			try {
				//is.close();
				servletOutputStream.close();
			} catch (Exception e) {
				log(e.getMessage(),e);
			}
		}
	}
	
	/** These are the "hop-by-hop" headers that should not be copied.
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html
     * I use an HttpClient HeaderGroup class instead of Set<String> because this
     * approach does case insensitive lookup faster.
     */
    protected static final HeaderGroup hopByHopHeaders;
    static {
    hopByHopHeaders = new HeaderGroup();
        String[] headers = new String[] {
                "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
                "TE", "Trailers", "Transfer-Encoding", "Upgrade" };
        for (String header : headers) {
            hopByHopHeaders.addHeader(new BasicHeader(header, null));
        }
    }

}
