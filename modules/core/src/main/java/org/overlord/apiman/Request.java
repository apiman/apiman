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
public interface Request extends Message {

	/**
	 * This method returns the API key associated with the request.
	 * 
	 * @return The API Key
	 */
	public String getAPIKey();
	
	/**
	 * This method returns the URI of the requested API.
	 * 
	 * @return The API URI
	 */
	public String getServiceName();
	
	/**
	 * This method returns the IP address of the requester.
	 * 
	 * @return The IP address
	 */
	public String getIPAddress();
	
	/**
	 * This method returns the URI for the target service.
	 * 
	 * @return The service URI
	 */
	public String getServiceURI();
	
	/**
	 * This method set the URI for the target service.
	 * 
	 * @param uri The service URI
	 */
	public void setServiceURI(String uri);
	
	/**
	 * This method returns the original URI used by the user.
	 * 
	 * @return The source URI
	 */
	public String getSourceURI();
	
	/**
	 * This method returns the operation.
	 * 
	 * @return The operation, or null if not defined
	 */
	public String getOperation();
	
	/**
	 * This method returns the named parameter value for the request.
	 * This assumes there is only a single value associated with
	 * the named parameter.
	 * 
	 * @param name The name of the parameter
	 * @return The parameter value, or null if not found
	 */
	public Object getParameter(String name);
	
	/**
	 * This method returns the list of parameters.
	 * 
	 * @return The parameter list
	 */
	public java.util.List<NameValuePair> getParameters();
	
}
