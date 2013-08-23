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

import org.overlord.apiman.model.Contract;
import org.overlord.apiman.model.Plan;
import org.overlord.apiman.model.Service;

/**
 * This interface represents the service presented to managers.
 *
 */
public interface ManagerService {

		 
	// TODO: Enable manager to retrieve usage information related to a service, broken down by
	// apps if the information is required
	
	public void registerService(Service service) throws Exception;
	
	public Service getService(String name) throws Exception;
	
	public java.util.List<String> getServiceNames() throws Exception;
	
	public void updateService(Service service) throws Exception;
	
	public void unregisterService(String name) throws Exception;

	public void registerPlan(Plan plan) throws Exception;
	
	public Plan getPlan(String id) throws Exception;
	
	public void updatePlan(Plan plan) throws Exception;
	
	public void unregisterPlan(String id) throws Exception;

	public void registerContract(Contract contract) throws Exception;
	
	public Contract getContract(String id) throws Exception;
	
	public void updateContract(Contract contract) throws Exception;
	
	public void unregisterContract(String id) throws Exception;

}
