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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.overlord.apiman.AbstractMessage;
import org.overlord.apiman.DefaultNameValuePair;
import org.overlord.apiman.Response;

public class HTTPGatewayResponse extends AbstractMessage implements Response {

	private HttpResponse _response=null;
	
	public HTTPGatewayResponse(HttpResponse resp) throws java.io.IOException {
		_response = resp;
		
		java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
		
		_response.getEntity().writeTo(baos);
		
		baos.close();
		
		setContent(baos.toByteArray());
		
		// Initialize headers
		Header[] values=resp.getAllHeaders();
		
		for (int i=0; i < values.length; i++) {
			getHeaders().add(new DefaultNameValuePair(values[i].getName(), values[i].getValue()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getSource() {
		return _response;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getStatusCode() {
		return (_response.getStatusLine().getStatusCode());
	}
	
}
