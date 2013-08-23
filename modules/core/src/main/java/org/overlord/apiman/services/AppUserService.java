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

/**
 * This interface represents the service presented to app users.
 *
 */
public interface AppUserService {

	/**
	 * This method enables an app user to register to use an app. If the app
	 * is configured to only allow the app's account manager to register users,
	 * then this operation will fail.
	 * 
	 * @param apiKey The API key of the app the user is registering to use
	 * @param appUserId The app user's id
	 * @param appUserPassword The app user's password
	 * @throws Exception Failed to register app user
	 */
	public void register(String apiKey, String appUserId, String appUserPassword) throws Exception;
	
	/**
	 * This method enables app users to be unregistered by the account user.
	 * 
	 * @param apiKey The API key of the app the user is registering to use
	 * @param appUserId The app user's id
	 * @throws Exception Failed to unregister app user
	 */
	public void removeAppUser(String apiKey, String appUserId) throws Exception;
	 
	// TODO: Enable app user to retrieve their usage information
	
}
