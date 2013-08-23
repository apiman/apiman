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
package org.overlord.apiman.inmemory.repository;

import java.util.Collections;

import org.overlord.apiman.model.Account;
import org.overlord.apiman.model.App;
import org.overlord.apiman.model.AppUser;
import org.overlord.apiman.model.Contract;
import org.overlord.apiman.model.Plan;
import org.overlord.apiman.model.Service;
import org.overlord.apiman.repository.APIManRepository;

public class InMemoryAPIManRepository implements APIManRepository {
	
	private static java.util.Map<String,Service> _services=new java.util.HashMap<String, Service>();
	private static java.util.Map<String,App> _apps=new java.util.HashMap<String, App>();
	private static java.util.Map<String,Plan> _plans=new java.util.HashMap<String, Plan>();
	private static java.util.Map<String,Contract> _contracts=new java.util.HashMap<String,Contract>();
	private static java.util.Map<String,Account> _accounts=new java.util.HashMap<String, Account>();
	private static java.util.Map<String,java.util.List<String>> _accountDomains=
			new java.util.HashMap<String, java.util.List<String>>();
	private static java.util.Map<String,java.util.Map<String,AppUser>> _domainAppUsers=
			new java.util.HashMap<String, java.util.Map<String,AppUser>>();
	
	public void createService(Service service) throws Exception {
		
		if (_services.containsKey(service.getName())) {
			throw new IllegalArgumentException("Service '"+service.getName()+"' already exists");
		}
		
		_services.put(service.getName(), service);
	}

	public void updateService(Service service) throws Exception {
		if (!_services.containsKey(service.getName())) {
			throw new Exception("Service does not exists for this id");
		}
		_services.put(service.getName(), service);
	}

	public void removeService(String name) throws Exception {
		if (!_services.containsKey(name)) {
			throw new Exception("Service does not exists for this id");
		}
		_services.remove(name);
	}

	public java.util.List<String> getServices() throws Exception {
		java.util.List<String> ret=new java.util.ArrayList<String>();
		
		ret.addAll(_services.keySet());
		
		Collections.sort(ret);
		
		return (ret);
	}
	
	public Service getService(String name) throws Exception {
		return (_services.get(name));
	}
	
	public void createApp(App app) throws Exception {
		if (app.getId() == null) {
			throw new Exception("App should have an id");
		}
		if (_apps.containsKey(app.getId())) {
			throw new Exception("App id already exists");
		}
		_apps.put(app.getId(), app);
	}

	public void updateApp(App app) throws Exception {
		if (!_apps.containsKey(app.getId())) {
			throw new Exception("App does not exists for this id");
		}
		_apps.put(app.getId(), app);
	}

	public void removeApp(App app) throws Exception {
		if (!_apps.containsKey(app.getId())) {
			throw new Exception("App does not exists for this id");
		}
		_apps.remove(app.getId());
	}

	public App getApp(String id) throws Exception {
		return (_apps.get(id));
	}
	
	public void linkAccountToDomain(Account account, String domainId) throws Exception {
		java.util.List<String> domainIds=_accountDomains.get(account.getUserId());
		
		if (domainIds == null) {
			domainIds = new java.util.ArrayList<String>();
			_accountDomains.put(account.getUserId(), domainIds);
		}
		
		domainIds.add(domainId);
	}

	public void unlinkAccountFromDomain(Account account, String domainId) throws Exception {
		java.util.List<String> domainIds=_accountDomains.get(account.getUserId());
		
		if (domainIds != null) {
			domainIds.remove(domainId);
		}
	}
	
	public java.util.List<String> getDomainsForAccount(Account account) throws Exception {
		return (_accountDomains.get(account.getUserId()));
	}

	public java.util.List<App> getAppsForDomain(String domainId) throws Exception {
		java.util.List<App> ret=new java.util.ArrayList<App>();
		
		for (App app: _apps.values()) {
			
			if (app.getDomainId().equals(domainId)) {
				ret.add(app);
			}
		}
		
		return (ret);
	}

	public void createAccount(Account account) throws Exception {
		if (_accounts.containsKey(account.getUserId())) {
			throw new Exception("Account already exists for this user id");
		}
		_accounts.put(account.getUserId(), account);
	}

	public void updateAccount(Account account) throws Exception {
		if (!_accounts.containsKey(account.getUserId())) {
			throw new Exception("Account does not exists for this user id");
		}
		_accounts.put(account.getUserId(), account);
	}

	public void removeAccount(String userId) throws Exception {
		if (!_accounts.containsKey(userId)) {
			throw new Exception("Account does not exists for this user id");
		}
		_accounts.remove(userId);
	}

	public Account getAccount(String userId) throws Exception {
		return (_accounts.get(userId));
	}

	public void createPlan(Plan plan) throws Exception {
		if (plan.getId() == null) {
			throw new Exception("Plan should have an id");
		}
		if (_plans.containsKey(plan.getId())) {
			throw new Exception("Plan id already exists");
		}
		
		_plans.put(plan.getId(), plan);
	}

	public void updatePlan(Plan plan) throws Exception {
		if (!_plans.containsKey(plan.getId())) {
			throw new Exception("Plan does not exists for this id");
		}
		_plans.put(plan.getId(), plan);
	}

	public void removePlan(String planId) throws Exception {
		if (!_plans.containsKey(planId)) {
			throw new Exception("Plan does not exists for this id");
		}
		_plans.remove(planId);
	}

	public java.util.List<Contract> getContracts(String apiKey, String service) throws Exception {
		java.util.List<Contract> ret=new java.util.ArrayList<Contract>();
		
		for (Contract c : _contracts.values()) {
			if (c.getAppId().equals(apiKey) && c.getService().equals(service)) {
				ret.add(c);
			}
		}

		return (ret);
	}

	public Plan getPlan(String apiKey, String serviceId) throws Exception {
		Plan ret=null;
		java.util.List<Contract> contracts=getContracts(apiKey, serviceId);
		
		long time=System.currentTimeMillis();
		
		for (int i=0; i < contracts.size(); i++) {
			Contract c=contracts.get(i);
			
			// Check that contract is valid
			if (c.startTime() < time && time < c.endTime()) {
				ret = getPlan(c.getPlanId());
			}
		}
		
		return (ret);
	}
	
	public Plan getPlan(String id) throws Exception {
		return (_plans.get(id));
	}

	public Contract getContract(String id) throws Exception {
		return (_contracts.get(id));
	}

	public void createContract(Contract contract) throws Exception {
		if (contract.getId() == null) {
			throw new Exception("Contract should have an id");
		}
		if (_contracts.containsKey(contract.getId())) {
			throw new Exception("Contract id already exists");
		}
		
		_contracts.put(contract.getId(), contract);
	}

	public void updateContract(Contract contract) throws Exception {
		if (!_contracts.containsKey(contract.getId())) {
			throw new Exception("Contract does not exists for this id");
		}
		_contracts.put(contract.getId(), contract);
	}

	public void removeContract(String contractId) throws Exception {
		if (!_contracts.containsKey(contractId)) {
			throw new Exception("Contract does not exists for this id");
		}
		_contracts.remove(contractId);
	}

	public java.util.List<Service> getServices(App app) throws Exception {
		java.util.List<Service> ret=new java.util.ArrayList<Service>();
		
		for (Contract c : _contracts.values()) {
			if (c.getAppId().equals(app.getId())) {
				Service s=getService(c.getService());
				
				if (s != null) {
					ret.add(s);					
				}
			}
		}
		
		return (ret);
	}

	public void createAppUser(AppUser appUser) throws Exception {
		java.util.Map<String,AppUser> appUsers=_domainAppUsers.get(appUser.getDomainId());
		
		if (appUsers == null) {
			appUsers = new java.util.HashMap<String, AppUser>();
			_domainAppUsers.put(appUser.getDomainId(), appUsers);
		}
		
		if (appUsers.containsKey(appUser.getUserId())) {
			throw new Exception("App user already exists for that id");
		}
		
		appUsers.put(appUser.getUserId(), appUser);
	}

	public AppUser getAppUser(String domainId, String userId) throws Exception {
		java.util.Map<String,AppUser> appUsers=_domainAppUsers.get(domainId);
		
		if (appUsers == null) {
			return (null);
		}
		
		return (appUsers.get(userId));
	}

	public void removeAppUser(String domainId, String userId) throws Exception {
		java.util.Map<String,AppUser> appUsers=_domainAppUsers.get(domainId);
		
		if (appUsers == null) {
			throw new Exception("Unknown domain");
		}
		
		if (!appUsers.containsKey(userId)) {
			throw new Exception("Unknown app user id");
		}
		
		appUsers.remove(userId);
	}

}
