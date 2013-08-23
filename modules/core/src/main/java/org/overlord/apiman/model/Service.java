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
package org.overlord.apiman.model;

/**
 * This class represents a deployed service being accessed via an API.
 *
 */
public class Service {

	private String _name=null;
	private String _uri=null;
	private java.util.List<String> _planIds=new java.util.ArrayList<String>();
	private java.util.List<Policy> _requestPolicies=new java.util.ArrayList<Policy>();
	private java.util.List<Policy> _responsePolicies=new java.util.ArrayList<Policy>();
	
	/**
	 * The default constructor.
	 */
	public Service() {
	}
	
	/**
	 * This method returns the name.
	 * 
	 * @return The name
	 */
	public String getName() {
		return (_name);
	}
	
	/**
	 * This method sets the name.
	 * 
	 * @param name The name
	 */
	public void setName(String name) {
		_name = name;
	}
	
	/**
	 * This method returns the URI for the service.
	 * 
	 * @return The URI
	 */
	public String getURI() {
		return (_uri);
	}
	
	/**
	 * This method sets the URI for the service.
	 * 
	 * @param uri The URI
	 */
	public void setURI(String uri) {
		_uri = uri;
	}
	
	/**
	 * This method determines whether a plan is required to
	 * access this service.
	 * 
	 * @return The plans associated with this service
	 */
	public java.util.List<String> getPlanIds() {
		return (_planIds);
	}
	
	/**
	 * This method returns the list of request policies that apply
	 * for this service.
	 * 
	 * @return The list of request policies
	 */
	public java.util.List<Policy> getRequestPolicies() {
		return (_requestPolicies);
	}
	
	/**
	 * This method returns the list of response policies that apply
	 * for this service.
	 * 
	 * @return The list of response policies
	 */
	public java.util.List<Policy> getResponsePolicies() {
		return (_responsePolicies);
	}
	
	/**
	 * This method determines whether policies have been defined.
	 * 
	 * @return Whether policies have been defined
	 */
	public boolean hasPolicies() {
		return (_requestPolicies.size() > 0 || _responsePolicies.size() > 0);
	}
}
