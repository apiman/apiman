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
package org.overlord.apiman.gateway;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.overlord.apiman.Request;

/**
 * This is an implementation of the Service Client Manager interface
 * used to represent a single service client.
 *
 */
public class DefaultServiceClientManager implements ServiceClientManager {

	@Inject @Any
	private Instance<ServiceClient> _initialServiceClients;
	
	private java.util.List<ServiceClient> _serviceClients=new java.util.ArrayList<ServiceClient>();
	
	/**
	 * The default constructor.
	 */
	public DefaultServiceClientManager() {
	}

	/**
	 * This method initializes the client manager.
	 */
	@PostConstruct
	public void init() {
		for (ServiceClient sc : _initialServiceClients) {
			_serviceClients.add(sc);
		}
	}
	
	/**
	 * This method returns the service clients.
	 * 
	 * @return The service clients
	 */
	public java.util.List<ServiceClient> getServiceClients() {
		return (_serviceClients);
	}

	/**
	 * {@inheritDoc}
	 */
	public ServiceClient getServiceClient(Request request) {
		for (int i=0; i < _serviceClients.size(); i++) {
			if (_serviceClients.get(i).isSupported(request)) {
				return (_serviceClients.get(i));
			}
		}
		return (null);
	}

}
