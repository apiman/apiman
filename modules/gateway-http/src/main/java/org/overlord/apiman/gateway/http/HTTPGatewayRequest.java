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

import javax.servlet.http.HttpServletRequest;

import org.overlord.apiman.AbstractRequest;
import org.overlord.apiman.DefaultNameValuePair;

/**
 * This class provides the HTTP request implementation of the gateway
 * request.
 *
 */
public class HTTPGatewayRequest extends AbstractRequest {

	private HttpServletRequest _request=null;
	private String _httpmethod=null;
	
	/**
	 * This constructor initializes the http servlet request.
	 * 
	 * @param req The request
	 */
	public HTTPGatewayRequest(HttpServletRequest req) throws Exception {
		_request = req;
		
		// Extract the service name
		setServiceName(extractServiceName(req.getPathInfo()));
		
		// Extract the operation
		setOperation(extractOperation(req.getPathInfo()));
		
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
		
		// Initialize parameters
		java.util.Enumeration<String> iter=req.getParameterNames();
		
		while (iter.hasMoreElements()) {
			String name=iter.nextElement();
			
			String[] values=req.getParameterValues(name);
			
			for (int i=0; i < values.length; i++) {
				getParameters().add(new DefaultNameValuePair(name, values[i]));
			}
		}
		
		// Initialize headers
		iter = req.getHeaderNames();
		
		while (iter.hasMoreElements()) {
			String name=iter.nextElement();
			
			java.util.Enumeration<String> values=req.getHeaders(name);
			
			while (values.hasMoreElements()) {
				getHeaders().add(new DefaultNameValuePair(name, values.nextElement()));
			}
		}
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
		String ret=_request.getParameter("apikey");
		
		if (ret == null) {
			ret = _request.getHeader("apikey");
		}
		
		return (ret);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSourceURI() {
		StringBuffer buf=_request.getRequestURL();
		
		if (_request.getQueryString() != null) {
			buf.append('?');
			buf.append(_request.getQueryString());
		}
		
		return (buf.toString());
	}

	public String getIPAddress() {
		return (_request.getRemoteAddr());
	}

	public String getHTTPMethod() {
		return (_httpmethod == null ? _request.getMethod() : _httpmethod);
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
