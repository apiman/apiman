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
package org.overlord.apiman.services;

import org.overlord.apiman.model.Account;
import org.overlord.apiman.model.App;
import org.overlord.apiman.model.AppUser;

public interface AccountService {

	public void createAccount(Account account) throws Exception;
	
	public Account getAccount(String userId) throws Exception;
	
	public void updateAccount(Account account) throws Exception;
	
	public void removeAccount(String userId) throws Exception;
	
	public String createDomain(Account account) throws Exception;
	
	public void removeDomain(Account account, String domainId) throws Exception;
	
	/**
	 * This method creates an app associated with the account of the supplied
	 * user id.
	 * 
	 * @param userId The app creator
	 * @param app The app to be created
	 * @throws Failed to create app
	 */
	public void createApp(String userId, App app) throws Exception;
	
	//public java.util.List<App> getApps(String userId) throws Exception;
	
	public void updateApp(String userId, App app) throws Exception;
	
	public void removeApp(String userId, String appId) throws Exception;
	
	/**
	 * This method enables app users to be registered by the account user.
	 * 
	 * @param userId The account user id
	 * @param appUser The app user details
	 * @throws Exception Failed to register app user
	 */
	public void createAppUser(String userId, AppUser appUser) throws Exception;
	
	/**
	 * This method enables app users to be unregistered by the account user.
	 * 
	 * @param userId The account user id
	 * @param apiKey The API key of the app the user is registering to use
	 * @param appUserId The app user's id
	 * @throws Exception Failed to unregister app user
	 */
	public void removeAppUser(String userId, String apiKey, String appUserId) throws Exception;
		 
	// TODO: Enable account user to retrieve usage information related to their app, broken down by
	// app user if the information is available/required

}
