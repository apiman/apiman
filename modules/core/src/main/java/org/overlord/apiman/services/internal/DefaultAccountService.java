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
package org.overlord.apiman.services.internal;

import java.util.UUID;

import javax.inject.Inject;

import org.overlord.apiman.model.Account;
import org.overlord.apiman.model.App;
import org.overlord.apiman.model.AppUser;
import org.overlord.apiman.repository.APIManRepository;
import org.overlord.apiman.services.AccountService;

public class DefaultAccountService implements AccountService {
	
	@Inject
	private APIManRepository _repository=null;
	
	/**
	 * This method sets the repository.
	 * 
	 * @param repo The repository
	 */
	public void setRepository(APIManRepository repo) {
	    _repository = repo;
	}
	
    /**
     * This method returns the repository.
     * 
     * @return The repository
     */
	public APIManRepository getRepository() {
	    return (_repository);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createAccount(Account account) throws Exception {
		// Check if account's user id already exists
		if (_repository.getAccount(account.getUserId()) != null) {
			throw new Exception("An account with user id '"+account.getUserId()+"' already exists");
		}
	
		_repository.createAccount(account);
				
		// Create default domain for account
		_repository.linkAccountToDomain(account, account.getUserId());
	}

	/**
	 * {@inheritDoc}
	 */
	public Account getAccount(String userId) throws Exception {
		return (_repository.getAccount(userId));
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateAccount(Account account) throws Exception {
		if (_repository.getAccount(account.getUserId()) == null) {
			throw new Exception("Unknown user id");
		}
		
		_repository.updateAccount(account);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAccount(String userId) throws Exception {
		Account account=_repository.getAccount(userId);
		
		_repository.unlinkAccountFromDomain(account, account.getUserId());
		
		_repository.removeAccount(userId);
	}

	/**
	 * {@inheritDoc}
	 */
	public String createDomain(Account account) throws Exception {
	
		// TODO Create unique id and link the user to it
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeDomain(Account account, String domainId) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void createApp(String userId, App app) throws Exception {
		
		Account account=_repository.getAccount(userId);
		
		if (account == null) {
			throw new Exception("Unknown user id");
		}
		
		// Check that the user is associated with the app's domain
		java.util.List<String> domains=_repository.getDomainsForAccount(account);
		
		if (app.getDomainId() == null) {
			throw new Exception("App must be associated with a domain");
		}
		
		if (domains == null || !domains.contains(app.getDomainId())) {
			throw new Exception("Account is not associated with app's domain");
		}
		
		// Check current list of apps for the domain
		java.util.List<App> apps=_repository.getAppsForDomain(app.getDomainId());
		
		for (App a : apps) {
			if (a.getName().equals(app.getName())) {
				throw new Exception("An app already exists with that name in this domain");
			}
		}
		
		if (app.getId() != null) {
			// Check not already defined
			if (_repository.getApp(app.getId()) != null) {
				throw new Exception("An app already exists with that id");
			}
		} else {
			app.setId(UUID.randomUUID().toString());
		}
		
		_repository.createApp(app);
	}

	/**
	 * {@inheritDoc}
	 */
	/*
	public List<App> getApps(String userId) throws Exception {
		Account account=_repository.getAccount(userId);
		
		if (account == null) {
			throw new Exception("Unknown user id");
		}
		
		java.util.List<App> ret=_repository.getApps(account);
		
		return (ret);
	}
	*/

	/**
	 * {@inheritDoc}
	 */
	public void updateApp(String userId, App app) throws Exception {
		Account account=_repository.getAccount(userId);
		
		if (account == null) {
			throw new Exception("Unknown user id");
		}
		
		// Check that the user is associated with the app's domain
		java.util.List<String> domains=_repository.getDomainsForAccount(account);
		
		// Get existing app
		App existing=_repository.getApp(app.getId());
		
		if (existing == null) {
			throw new Exception("App does not exist in repository");
		}
		
		// Check domain has not been changed
		if (!existing.getDomainId().equals(app.getDomainId())) {
			throw new Exception("App's domain cannot be changed");
		}
		
		if (domains == null || !domains.contains(existing.getDomainId())) {
			throw new Exception("Account is not associated with app's domain");
		}
		
		_repository.updateApp(app);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeApp(String userId, String appId) throws Exception {
		Account account=_repository.getAccount(userId);
		
		if (account == null) {
			throw new Exception("Unknown user id");
		}
		
		// Check that the user is associated with the app's domain
		java.util.List<String> domains=_repository.getDomainsForAccount(account);
		
		// Get existing app
		App existing=_repository.getApp(appId);
		
		if (existing == null) {
			throw new Exception("App does not exist in repository");
		}
		
		if (domains == null || !domains.contains(existing.getDomainId())) {
			throw new Exception("Account is not associated with app's domain");
		}
		
		_repository.removeApp(existing);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createAppUser(String userId, AppUser appUser) throws Exception {
		Account account=_repository.getAccount(userId);
		
		if (account == null) {
			throw new Exception("Unknown user id");
		}
		
		// Check that the user is associated with the app's domain
		java.util.List<String> domains=_repository.getDomainsForAccount(account);
		
		if (appUser.getDomainId() == null) {
			throw new Exception("App user must be associated with a domain");
		}
		
		if (domains == null || !domains.contains(appUser.getDomainId())) {
			throw new Exception("Account is not associated with app user's domain");
		}
		
		_repository.createAppUser(appUser);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAppUser(String userId, String apiKey, String appUserId)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
