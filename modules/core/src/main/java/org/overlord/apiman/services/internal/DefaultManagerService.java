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

import java.util.List;

import javax.inject.Inject;

import org.overlord.apiman.model.Contract;
import org.overlord.apiman.model.Plan;
import org.overlord.apiman.model.Service;
import org.overlord.apiman.repository.APIManRepository;
import org.overlord.apiman.services.ManagerService;

public class DefaultManagerService implements ManagerService {

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

	public void registerService(Service service) throws Exception {
		if (_repository.getService(service.getName()) != null) {
			throw new Exception("A service already exists with name '"+service.getName()+"'");
		}
		
		_repository.createService(service);
	}

	public Service getService(String name) throws Exception {
		return (_repository.getService(name));
	}

	public List<String> getServiceNames() throws Exception {
		return (_repository.getServices());
	}

	public void updateService(Service service) throws Exception {
		_repository.updateService(service);
	}

	public void unregisterService(String name) throws Exception {
		if (_repository.getService(name) == null) {
			throw new Exception("Service with name '"+name+"' does not exist");
		}
		
		_repository.removeService(name);
	}

	public void registerPlan(Plan plan) throws Exception {
		_repository.createPlan(plan);
	}

	public Plan getPlan(String id) throws Exception {
		return (_repository.getPlan(id));
	}
	
	public void updatePlan(Plan plan) throws Exception {
		_repository.updatePlan(plan);
	}

	public void unregisterPlan(String id) throws Exception {
		if (_repository.getPlan(id) == null) {
			throw new Exception("Plan with that id does not exist");
		}
		
		_repository.removePlan(id);
	}


	public void registerContract(Contract contract) throws Exception {
		// Check contract details
		
		if (contract.getAppId() == null) {
			throw new Exception("Contract must define an app id");
		}
		
		if (_repository.getApp(contract.getAppId()) == null) {
			throw new Exception("App with id '"+contract.getAppId()+"' does not exist");
		}
		
		if (contract.getService() == null) {
			throw new Exception("Contract must define a service");
		}
		
		Service s=_repository.getService(contract.getService());
		
		if (s == null) {
			throw new Exception("Service '"+contract.getService()+"' does not exist");
		}
				
		if (contract.getPlanId() == null) {
			throw new Exception("Contract must define a plan id");
		}
		
		if (_repository.getPlan(contract.getPlanId()) == null) {
			throw new Exception("Plan with id '"+contract.getPlanId()+"' does not exist");
		}
		
		if (!s.getPlanIds().contains(contract.getPlanId())) {
			throw new Exception("Service '"+contract.getService()+
					"' is not associated with plan id '"+contract.getPlanId()+"'");
		}

		_repository.createContract(contract);
	}

	public Contract getContract(String id) throws Exception {
		return (_repository.getContract(id));
	}
	
	public void updateContract(Contract contract) throws Exception {
		_repository.updateContract(contract);
	}

	public void unregisterContract(String id) throws Exception {
		if (_repository.getContract(id) == null) {
			throw new Exception("Contract with that id does not exist");
		}
		
		_repository.removeContract(id);
	}

}
