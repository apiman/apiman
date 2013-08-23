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
 * This interface represents a gateway request or response.
 *
 */
public interface Message {

	/**
	 * This method returns the source representation for the message.
	 * 
	 * @return The source
	 */
	public Object getSource();
	
	/**
	 * This method returns the content of the message.
	 * 
	 * @return The content
	 */
	public byte[] getContent();
	
	/**
	 * This method sets the content.
	 * 
	 * @param content The content
	 */
	public void setContent(byte[] content);
	
	/**
	 * This method returns the named header value for the message.
	 * 
	 * @param name The name of the header property
	 * @return The header value, or null if not found
	 */
	public Object getHeader(String name);
	
	/**
	 * This method returns the list of headers.
	 * 
	 * @return The headers
	 */
	public java.util.List<NameValuePair> getHeaders();
	
	/**
	 * This method returns the context properties carried with
	 * the message during processing.
	 * 
	 * @return The context properties
	 */
	public java.util.Map<String, String> getContext();
	
}
