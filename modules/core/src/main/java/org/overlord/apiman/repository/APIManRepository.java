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
package org.overlord.apiman.repository;

import org.overlord.apiman.model.Account;
import org.overlord.apiman.model.App;
import org.overlord.apiman.model.AppUser;
import org.overlord.apiman.model.Contract;
import org.overlord.apiman.model.Plan;
import org.overlord.apiman.model.Service;

/**
 * This interface provides access to the API management repository.
 *
 */
public interface APIManRepository {

	public void createService(Service service) throws Exception;
	
	public void updateService(Service service) throws Exception;
	
	public void removeService(String name) throws Exception;
	
	public Service getService(String name) throws Exception;
	
	/**
	 * This method returns the list of available service names.
	 * 
	 * @return The service names
	 * @throws Exception Failed to retrieve the list of service names
	 */
	public java.util.List<String> getServices() throws Exception;
	
	/**
	 * This method returns the list of services available to the
	 * supplied app.
	 * 
	 * @param app The app
	 * @return The list of services
	 * @throws Exception Failed to get list of services
	 */
	public java.util.List<Service> getServices(App app) throws Exception;
	
	public void createApp(App app) throws Exception;
	
	public void updateApp(App app) throws Exception;
	
	public void removeApp(App app) throws Exception;
	
	public App getApp(String id) throws Exception;
	
	public void linkAccountToDomain(Account account, String domainId) throws Exception;
	
	public void unlinkAccountFromDomain(Account account, String domainId) throws Exception;
	
	public java.util.List<String> getDomainsForAccount(Account account) throws Exception;
	
	public java.util.List<App> getAppsForDomain(String domainId) throws Exception;
	
	public void createAccount(Account account) throws Exception;
	
	public void updateAccount(Account account) throws Exception;
	
	public void removeAccount(String userId) throws Exception;
	
	public Account getAccount(String userId) throws Exception;
	
	public Plan getPlan(String id) throws Exception;
	
	public void createPlan(Plan plan) throws Exception;
	
	public void updatePlan(Plan plan) throws Exception;
	
	public void removePlan(String planId) throws Exception;
	
	public java.util.List<Contract> getContracts(String apiKey, String service) throws Exception;
	
	public Plan getPlan(String apiKey, String service) throws Exception;
	
	public Contract getContract(String id) throws Exception;

	public void createContract(Contract contract) throws Exception;
	
	public void updateContract(Contract contract) throws Exception;
	
	public void removeContract(String contractId) throws Exception;
	
	public void createAppUser(AppUser appUser) throws Exception;
	
	public AppUser getAppUser(String domainId, String userId) throws Exception;
	
	public void removeAppUser(String domainId, String userId) throws Exception;
	
}
