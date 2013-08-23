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
package org.overlord.apiman;

/**
 * This class provides an abstract implementation of the message interface.
 *
 */
public abstract class AbstractMessage implements Message {

	private byte[] _content=null;
	private java.util.List<NameValuePair> _headers=new java.util.ArrayList<NameValuePair>();
	private java.util.Map<String,String> _context=new java.util.HashMap<String, String>();

	public byte[] getContent() {
		return (_content);
	}
	
	public void setContent(byte[] content) {
		_content = content;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getHeader(String name) {
		for (int i=0; i < _headers.size(); i++) {
			NameValuePair nvp=_headers.get(i);
			if (nvp.getName().equalsIgnoreCase(name)) {
				return (nvp.getValue());
			}
		}
		
		return (null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public java.util.List<NameValuePair> getHeaders() {
		return (_headers);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public java.util.Map<String, String> getContext() {
		return (_context);
	}
	
}
