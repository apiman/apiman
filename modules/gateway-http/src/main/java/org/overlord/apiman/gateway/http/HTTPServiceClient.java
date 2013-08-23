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

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.overlord.apiman.Request;
import org.overlord.apiman.Response;
import org.overlord.apiman.gateway.ServiceClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The HTTP based implementation of the service client.
 * 
 * Based on the http servlet proxy implemented by David Smiley:
 * https://github.com/dsmiley/HTTP-Proxy-Servlet
 *
 */
public class HTTPServiceClient implements ServiceClient {
	
	private static final Logger LOG=Logger.getLogger(HTTPServiceClient.class.getName());

	private static final String APIKEY = "apikey";

	private HttpClient proxyClient;
	
	/**
	 * The default constructor.
	 */
	public HTTPServiceClient() {
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isSupported(Request request) {
		return (request.getServiceURI().startsWith("http:") ||
				request.getServiceURI().startsWith("https:"));
	}

	/**
	 * {@inheritDoc}
	 */
	@PostConstruct
	public void init() {
		HttpParams hcParams = new BasicHttpParams();
		//readConfigParam(hcParams, ClientPNames.HANDLE_REDIRECTS, Boolean.class);
		proxyClient = new DefaultHttpClient(new ThreadSafeClientConnManager(),hcParams);
	}

	/**
	 * {@inheritDoc}
	 */
	public Response process(Request request) throws Exception {
	    String method="GET";
	    
	    if (request instanceof HTTPGatewayRequest) {
	    		method = ((HTTPGatewayRequest)request).getHTTPMethod();
	    }
	    
	    String proxyRequestUri = rewriteUrlFromRequest(request);
	    
	    HttpRequest proxyRequest;
	    
	    //spec: RFC 2616, sec 4.3: either of these two headers signal that there is a message body.
	    if (request.getHeader(HttpHeaders.CONTENT_LENGTH) != null ||
	    		request.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
	    	HttpEntityEnclosingRequest eProxyRequest = new BasicHttpEntityEnclosingRequest(method, proxyRequestUri);
	    	
	    	java.io.InputStream is=new java.io.ByteArrayInputStream(request.getContent());
	    	
	    	InputStreamEntity entity=new InputStreamEntity(is, request.getContent().length);
	    	
	    	is.close();
	    	
	    	eProxyRequest.setEntity(entity);

	    	proxyRequest = eProxyRequest;
	    } else {
	    	proxyRequest = new BasicHttpRequest(method, proxyRequestUri);
	    }

	    copyRequestHeaders(request, proxyRequest);

	    try {
	    	// Execute the request
	    	if (LOG.isLoggable(Level.FINER)) {
	    		LOG.finer("proxy " + method + " uri: " + request.getSourceURI() + " -- "
	    					+ proxyRequest.getRequestLine().getUri());
	    	}

	    	HttpResponse proxyResponse = proxyClient.execute(URIUtils.extractHost(
	    			new java.net.URI(request.getServiceURI())), proxyRequest);
	    	
	    	Response resp=new HTTPGatewayResponse((HttpResponse)proxyResponse);
	    	
	    	return (resp);

	    } catch (Exception e) {
	    	//abort request, according to best practice with HttpClient
	    	if (proxyRequest instanceof AbortableHttpRequest) {
	    		AbortableHttpRequest abortableHttpRequest = (AbortableHttpRequest) proxyRequest;
	    		abortableHttpRequest.abort();
	    	}
	    	if (e instanceof RuntimeException) {
	    		throw (RuntimeException)e;
	    	}
	    	if (e instanceof ServletException) {
	    		throw (ServletException)e;
	    	}
	    	//noinspection ConstantConditions
	    	if (e instanceof IOException) {
	    		throw (IOException) e;
	    	}
	    	throw new RuntimeException(e);
	    }
	}

	@PreDestroy
	public void destroy() {
		if (proxyClient != null) {
			proxyClient.getConnectionManager().shutdown();
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

	/** Copy request headers from the servlet client to the proxy request. */
	protected void copyRequestHeaders(Request request, HttpRequest proxyRequest) throws Exception {

		for (int i=0; i < request.getHeaders().size(); i++) {
			org.overlord.apiman.NameValuePair nvp=request.getHeaders().get(i);
			
			if (nvp.getName().equalsIgnoreCase(APIKEY)) {
				continue;
			}
			//Instead the content-length is effectively set via InputStreamEntity
			if (nvp.getName().equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
				continue;
			}
			if (hopByHopHeaders.containsHeader(nvp.getName())) {
				continue;
			}

			// In case the proxy host is running multiple virtual servers,
			// rewrite the Host header to ensure that we get content from
			// the correct virtual server
			
			// TODO: Assumes string for now
			String headerValue=(String)nvp.getValue();
			
			if (nvp.getName().equalsIgnoreCase(HttpHeaders.HOST)) {
				HttpHost host = URIUtils.extractHost(new java.net.URI(request.getServiceURI()));
				headerValue = host.getHostName();
				if (host.getPort() != -1) {
					headerValue += ":"+host.getPort();
				}
			}
			
			proxyRequest.addHeader(nvp.getName(), headerValue);
		}
	}
	
	/** Reads the request URI from {@code servletRequest} and rewrites it, considering {@link
	 * #targetUri}. It's used to make the new request.
	 */
	protected String rewriteUrlFromRequest(Request request) {
		StringBuilder uri = new StringBuilder(500);
		
		uri.append(request.getServiceURI().toString());
		
		// Append optional operation
		if (request.getOperation() != null) {
			
			if (uri.charAt(uri.length()-1) != '/') {
				uri.append('/');				
			}
			
			uri.append(request.getOperation());
		}
		
		// Handle the query string
		if (request.getParameters().size() > 0) {
			uri.append('?');
			
			boolean f_first=true;

			for (int i=0; i < request.getParameters().size(); i++) {
				org.overlord.apiman.NameValuePair nvp=request.getParameters().get(i);
				
				// Skip api key
				if (nvp.getName().equalsIgnoreCase(APIKEY)) {
					continue;
				}
				
				if (!f_first) {
					uri.append('&');
				}
				
				uri.append(nvp.getName());
				uri.append('=');
				uri.append(nvp.getValue().toString());
				
				f_first = false;
			}
		}
		/*
		String queryString = request.getQueryString();//ex:(following '?'): name=value&foo=bar#fragment
		if (queryString != null && queryString.length() > 0) {
			uri.append('?');
			int fragIdx = queryString.indexOf('#');
			String queryNoFrag = (fragIdx < 0 ? queryString : queryString.substring(0,fragIdx));
			uri.append(encodeUriQuery(queryNoFrag));
			if (fragIdx >= 0) {
				uri.append('#');
				uri.append(encodeUriQuery(queryString.substring(fragIdx + 1)));
			}
		}
		*/
		
		return uri.toString();
	}

	/**
	 * Encodes characters in the query or fragment part of the URI.
	 *
	 * <p>Unfortunately, an incoming URI sometimes has characters disallowed by the spec.  HttpClient
	 * insists that the outgoing proxied request has a valid URI because it uses Java's {@link URI}.
	 * To be more forgiving, we must escape the problematic characters.  See the URI class for the
	 * spec.
	 *
	 * @param in example: name=value&foo=bar#fragment
	 */
	/*
	protected static CharSequence encodeUriQuery(CharSequence in) {
		//Note that I can't simply use URI.java to encode because it will escape pre-existing escaped things.
		StringBuilder outBuf = null;
		Formatter formatter = null;
		for(int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			boolean escape = true;
			if (c < 128) {
				if (asciiQueryChars.get((int)c)) {
					escape = false;
				}
			} else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) {//not-ascii
				escape = false;
			}
			if (!escape) {
				if (outBuf != null) {
					outBuf.append(c);
				}
			} else {
				//escape
				if (outBuf == null) {
					outBuf = new StringBuilder(in.length() + 5*3);
					outBuf.append(in,0,i);
					formatter = new Formatter(outBuf);
				}
				//leading %, 0 padded, width 2, capital hex
				formatter.format("%%%02X",(int)c);//TODO
			}
		}
		if (formatter != null) {
			formatter.close();
		}
		return outBuf != null ? outBuf : in;
	}

	protected static final BitSet asciiQueryChars;
	static {
		char[] c_unreserved = "_-!.~'()*".toCharArray();//plus alphanum
		char[] c_punct = ",;:$&+=".toCharArray();
		char[] c_reserved = "?/[]@".toCharArray();//plus punct

		asciiQueryChars = new BitSet(128);
		for(char c = 'a'; c <= 'z'; c++) asciiQueryChars.set((int)c);
		for(char c = 'A'; c <= 'Z'; c++) asciiQueryChars.set((int)c);
		for(char c = '0'; c <= '9'; c++) asciiQueryChars.set((int)c);
		for(char c : c_unreserved) asciiQueryChars.set((int)c);
		for(char c : c_punct) asciiQueryChars.set((int)c);
		for(char c : c_reserved) asciiQueryChars.set((int)c);

		asciiQueryChars.set((int)'%');//leave existing percent escapes in place
	}
	*/
}
