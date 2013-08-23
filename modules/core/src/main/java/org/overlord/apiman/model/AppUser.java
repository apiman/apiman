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

public class AppUser {

	private String _userId=null;
	private String _password=null;
	private String _domainId=null;
	
	/**
	 * The default constructor.
	 */
	public AppUser() {
	}
	
	/**
	 * This method returns the user id.
	 * 
	 * @return The user id
	 */
	public String getUserId() {
		return (_userId);
	}
	
	/**
	 * This method sets the user id.
	 * 
	 * @param userId The user id
	 */
	public void setUserId(String userId) {
		_userId = userId;
	}
	
	/**
	 * This method returns the password.
	 * 
	 * @return The password
	 */
	public String getPassword() {
		return (_password);
	}
	
	/**
	 * This method sets the password.
	 * 
	 * @param password The password
	 */
	public void setPassword(String password) {
		_password = password;
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

}
