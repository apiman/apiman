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

import io.undertow.server.HttpServerExchange;

import org.overlord.apiman.AbstractRequest;
import org.overlord.apiman.DefaultNameValuePair;

/**
 * This class provides the HTTP request implementation of the gateway
 * request.
 *
 */
public class UndertowGatewayRequest extends AbstractRequest {

	private HttpServerExchange _request=null;
	private String _httpmethod=null;
	
	/**
	 * This constructor initializes the http servlet request.
	 * 
	 * @param req The request
	 */
	public UndertowGatewayRequest(HttpServerExchange req) throws Exception {
		_request = req;
		
		// Extract the service name
		setServiceName(extractServiceName(req.getRelativePath()));
		
		// Extract the operation
		setOperation(extractOperation(req.getRelativePath()));

		if (!req.getRequestMethod().toString().equals("GET")) {
    		// Transfer content to byte array
    		java.io.InputStream is=req.getInputStream();
    		java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
    				
    		while (true) {
    			byte[] b=new byte[10240];
    			
    			int len=is.read(b);
    			
    			if (len == -1) {
    				break;
    			}
    			
    			baos.write(b, 0, len);
    		}
    		
    		is.close();
    		baos.close();
    		
    		setContent(baos.toByteArray());
		}

		// Initialize parameters
		java.util.Iterator<String> iter=req.getQueryParameters().keySet().iterator();
		
		while (iter.hasNext()) {
			String name=iter.next();
			
			java.util.Deque<String> values=req.getQueryParameters().get(name);

			while (values.size() > 0) {
				getParameters().add(new DefaultNameValuePair(name, values.poll()));
			}
		}
		
		// Initialize headers
		/* TODO: Transfer header info
		java.util.Iterator<HeaderValues> headerIter = req.getRequestHeaders().iterator();
		
		while (headerIter.hasNext()) {
			HeaderValues hv=headerIter.next();
			
			java.util.Enumeration<String> values=req.getHeaders(name);
			
			while (values.hasMoreElements()) {
				getHeaders().add(new DefaultNameValuePair(name, values.nextElement()));
			}
		}
		*/
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getSource() {
		return _request;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAPIKey() {
		String ret=(String)getParameter("apikey");//_request.getParameter("apikey");
		
		/*
		if (ret == null) {
			ret = _request.getHeader("apikey");
		}
		*/
		
		return (ret);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSourceURI() {
		StringBuffer buf=new StringBuffer(_request.getRequestURL());
		
		if (_request.getQueryString() != null) {
			buf.append('?');
			buf.append(_request.getQueryString());
		}
		
		return (buf.toString());
	}

	public String getIPAddress() {
	    
	    // TODO:
		return (null);
	}

	public String getHTTPMethod() {
		return (_httpmethod == null ? _request.getRequestMethod().toString() : _httpmethod);
	}

	public void setHTTPMethod(String method) {
		_httpmethod = method;
	}

	protected static String extractServiceName(String pathInfo) {
		String ret=pathInfo;
		
		int ind=pathInfo.indexOf('/', 1);
		
		if (ind != -1) {
			ret = pathInfo.substring(1, ind);
		}
		
		return (ret);
	}
	
	protected static String extractOperation(String pathInfo) {
		String ret=null;
		
		int ind=pathInfo.indexOf('/', 1);
		
		if (ind != -1) {
			ret = pathInfo.substring(ind+1);
		}
		
		return (ret);
	}
}
