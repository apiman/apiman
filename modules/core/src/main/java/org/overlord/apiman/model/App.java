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
 * This class represents a client application.
 *
 */
public class App {

	private String _id=null;
	private String _name=null;
	private String _domainId=null;
	private java.util.List<Policy> _requestPolicies=new java.util.ArrayList<Policy>();
	private java.util.List<Policy> _responsePolicies=new java.util.ArrayList<Policy>();
	
	/**
	 * The default constructor.
	 */
	public App() {
	}
	
	/**
	 * This method returns the id.
	 * 
	 * @return The id
	 */
	public String getId() {
		return (_id);
	}
	
	/**
	 * This method sets the id.
	 * 
	 * @param id The id
	 */
	public void setId(String id) {
		_id = id;
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
	 * This method returns the domain.
	 * 
	 * @return The domain id
	 */
	public String getDomainId() {
		return (_domainId);
	}
	
	/**
	 * This method sets the domain.
	 * 
	 * @param domainId The domain id
	 */
	public void setDomainId(String domainId) {
		_domainId = domainId;
	}
	
	/**
	 * This method returns the list of request policies that apply
	 * for this app.
	 * 
	 * @return The list of request policies
	 */
	public java.util.List<Policy> getRequestPolicies() {
		return (_requestPolicies);
	}
	
	/**
	 * This method returns the list of response policies that apply
	 * for this app.
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
