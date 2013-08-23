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

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.overlord.apiman.model.Policy;

public class DefaultPolicyManager implements PolicyManager {
	
	@Inject @Any
	private Instance<PolicyHandler> _detectedHandlers;	
	
	private java.util.Map<Class<? extends Policy>, PolicyHandler> _handlers=
					new java.util.HashMap<Class<? extends Policy>, PolicyHandler>();
	
	/**
	 * The default constructor.
	 */
	public DefaultPolicyManager() {
	}
	
	@PostConstruct
	public void init() {
		
		for (PolicyHandler ph : _detectedHandlers) {
			_handlers.put(ph.getPolicyType(), ph);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public PolicyHandler getPolicyHandler(Policy policy) {
		return (_handlers.get(policy.getClass()));
	}
}
