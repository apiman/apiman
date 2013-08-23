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

import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.overlord.apiman.Request;
import org.overlord.apiman.Response;
import org.overlord.apiman.model.App;
import org.overlord.apiman.model.Plan;
import org.overlord.apiman.model.Policy;
import org.overlord.apiman.model.Service;
import org.overlord.apiman.policy.DefaultPolicyContext;
import org.overlord.apiman.policy.PolicyContext;
import org.overlord.apiman.policy.PolicyHandler;
import org.overlord.apiman.policy.PolicyManager;
import org.overlord.apiman.repository.APIManRepository;

/**
 * This class represents a default gateway implementation.
 *
 */
public class DefaultGateway implements Gateway {
	
	private static final Logger LOG=Logger.getLogger(DefaultGateway.class.getName());
	
	@Inject @Dependent
	private ServiceClientManager _serviceClientManager=null;
	
	@Inject @Dependent
	private APIManRepository _apiRepository=null;
	
	@Inject @Dependent
	private PolicyManager _policyManager=null;
	
	/**
	 * The default constructor.
	 */
	public DefaultGateway() {
	}
	
	/**
	 * This method sets the service client manager.
	 * 
	 * @param manager The service client manager
	 */
	public void setServiceClientManager(ServiceClientManager manager) {
		_serviceClientManager = manager;
	}
	
	/**
	 * This method returns the service client manager.
	 * 
	 * @return The service client manager
	 */
	public ServiceClientManager getServiceClient() {
		return (_serviceClientManager);
	}

	/**
	 * {@inheritDoc}
	 */
	public Response process(Request request) throws Exception {
		Response ret=null;
		
		// Identify calling app
		String apiKey=request.getAPIKey();
		
		if (apiKey == null) {
			throw new Exception("No API key provided with request");
		}
		
		App app=_apiRepository.getApp(apiKey);
		
		if (app == null) {
			throw new Exception("No app found for key '"+apiKey+"'");
		}
				
		// Identify target service configuration
		Service service=_apiRepository.getService(request.getServiceName());
		
		if (service == null) {
			throw new Exception("No service found for name '"+request.getServiceName()+"'");
		}
		
		// Identify a plan for the app and service
		Plan plan=null;
		
		// If the service has associated plans, then the app must be linked
		// to the service via a plan. If no plan is defined for the service, then
		// can be more dynamic and allow apps to access it without any direct
		// connection.
		if (service.getPlanIds().size() > 0) {
			plan = _apiRepository.getPlan(apiKey, service.getName());
			
			if (plan == null) {
				throw new Exception("Your application does not have a plan setup for this service");
			}
		}
		
		//if (LOG.isLoggable(Level.FINEST)) {
			LOG.info("Service URI="+service.getURI());
		//}
		
		request.setServiceURI(service.getURI());
		
		// Create policy context - but only if required
		PolicyContext context=null;
		
		if (app.hasPolicies() || (plan != null && plan.hasPolicies()) || service.hasPolicies()) {
			context = new DefaultPolicyContext();
			((DefaultPolicyContext)context).setRepository(_apiRepository);
			((DefaultPolicyContext)context).setDomainId(app.getDomainId());
		}
		
		// Process the request with the policies associated with the app
		for (int i=0; i < app.getRequestPolicies().size(); i++) {
			Policy policy=app.getRequestPolicies().get(i);
			PolicyHandler handler=_policyManager.getPolicyHandler(policy);
			
			if (handler == null) {
				throw new Exception("Policy handler not found for policy '"+policy+"'");
			}
			
			//if (LOG.isLoggable(Level.FINEST)) {
				LOG.info("App["+app+"] request policy="+policy+" request="+request);
			//}
				
			handler.apply(context, policy, request);
		}
		
		if (plan != null) {
			for (int i=0; i < plan.getRequestPolicies().size(); i++) {
				Policy policy=plan.getRequestPolicies().get(i);
				PolicyHandler handler=_policyManager.getPolicyHandler(policy);
				
				if (handler == null) {
					throw new Exception("Policy handler not found for policy '"+policy+"'");
				}
				
				//if (LOG.isLoggable(Level.FINEST)) {
					LOG.info("Plan["+plan+"] request policy="+policy+" request="+request);
				//}
				
				handler.apply(context, policy, request);
			}
		}

		// Process the request with the policies associated with the service
		for (int i=0; i < service.getRequestPolicies().size(); i++) {
			Policy policy=service.getRequestPolicies().get(i);
			PolicyHandler handler=_policyManager.getPolicyHandler(policy);
			
			if (handler == null) {
				throw new Exception("Policy handler not found for policy '"+policy+"'");
			}
			
			//if (LOG.isLoggable(Level.FINEST)) {
				LOG.info("Service["+service+"] request policy="+policy+" request="+request);
			//}
		
			handler.apply(context, policy, request);
		}
		
		ServiceClient serviceClient=_serviceClientManager.getServiceClient(request);
		
		if (serviceClient != null) {
			ret = serviceClient.process(request);
		} else {
			throw new Exception("No service client for request URI '"+request.getServiceURI()+"'");
		}
		
		if (ret != null) {
			// Process the response with the policies associated with the service
			for (int i=0; i < service.getResponsePolicies().size(); i++) {
				Policy policy=service.getResponsePolicies().get(i);
				PolicyHandler handler=_policyManager.getPolicyHandler(policy);
				
				if (handler == null) {
					throw new Exception("Policy handler not found for policy '"+policy+"'");
				}
				
				//if (LOG.isLoggable(Level.FINEST)) {
					LOG.info("Service["+service+"] response policy="+policy+" ret="+ret);
				//}
		
				handler.apply(context, policy, ret);
			}
			
			if (plan != null) {
				for (int i=0; i < plan.getResponsePolicies().size(); i++) {
					Policy policy=plan.getResponsePolicies().get(i);
					PolicyHandler handler=_policyManager.getPolicyHandler(policy);
					
					if (handler == null) {
						throw new Exception("Policy handler not found for policy '"+policy+"'");
					}
					
					//if (LOG.isLoggable(Level.FINEST)) {
						LOG.info("Plan["+plan+"] response policy="+policy+" ret="+ret);
					//}
				
					handler.apply(context, policy, ret);
				}
			}

			// Process the response with the policies associated with the app
			for (int i=0; i < app.getResponsePolicies().size(); i++) {
				Policy policy=app.getResponsePolicies().get(i);
				PolicyHandler handler=_policyManager.getPolicyHandler(policy);
				
				if (handler == null) {
					throw new Exception("Policy handler not found for policy '"+policy+"'");
				}
				
				//if (LOG.isLoggable(Level.FINEST)) {
					LOG.info("App["+app+"] response policy="+policy+" ret="+ret);
				//}
				
				handler.apply(context, policy, ret);
			}
			
		}
		
		//if (LOG.isLoggable(Level.FINEST)) {
			LOG.info("Gateway response="+ret);
		//}
		
		return (ret);
	}
	
}
