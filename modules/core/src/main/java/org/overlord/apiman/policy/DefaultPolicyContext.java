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
package org.overlord.apiman.policy;

import org.overlord.apiman.model.AppUser;
import org.overlord.apiman.repository.APIManRepository;

/**
 * This class provides the default implementation of the policy
 * context.
 *
 */
public class DefaultPolicyContext implements PolicyContext {
	
	private APIManRepository _repository=null;
	
	private String _domainId=null;
	
	/**
	 * This method sets the repository.
	 * 
	 * @param repository The repository
	 */
	public void setRepository(APIManRepository repository) {
		_repository = repository;
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
	 * This method sets the domain id associated with the current
	 * invocation.
	 * 
	 * @param domainId The domain id
	 */
	public void setDomainId(String domainId) {
		_domainId = domainId;
	}
	
	/**
	 * This method returns the domain id associated with the current
	 * invocation.
	 * 
	 * @return The domain id
	 */
	public String getDomainId() {
		return (_domainId);
	}

	/**
	 * {@inheritDoc}
	 */
	public AppUser getAppUser(String username) throws Exception {
		return (_repository.getAppUser(_domainId, username));
	}
	
}
