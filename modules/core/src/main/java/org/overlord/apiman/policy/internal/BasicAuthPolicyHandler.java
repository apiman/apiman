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
package org.overlord.apiman.policy.internal;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.overlord.apiman.Message;
import org.overlord.apiman.model.AppUser;
import org.overlord.apiman.model.Policy;
import org.overlord.apiman.model.policies.BasicAuthPolicy;
import org.overlord.apiman.policy.PolicyHandler;
import org.overlord.apiman.policy.PolicyContext;
import org.overlord.apiman.util.AuthorizationUtil;

/**
 * This policy implements basic authentication.
 *
 */
public class BasicAuthPolicyHandler implements PolicyHandler {
	
	private static final Logger LOG=Logger.getLogger(BasicAuthPolicyHandler.class.getName());
	
	/**
	 * The default constructor.
	 */
	public BasicAuthPolicyHandler() {
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<? extends Policy> getPolicyType() {
		return (BasicAuthPolicy.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public void apply(PolicyContext context, Policy policy, Message mesg) throws Exception {	
		BasicAuthPolicy pol=(BasicAuthPolicy)policy;
		
		String auth=(String)mesg.getHeader("Authorization");
		
		if (auth == null) {
			throw new Exception("No authorization information available");
		}
		
		String[] up=AuthorizationUtil.getBasicAuthUsernamePassword(auth);
		
		if (pol.isAuthenticate()) {
			
			// Check if user is known
			AppUser appUser=context.getAppUser(up[0]);
			
			if (appUser == null) {
				throw new Exception("Unknown app user '"+up[0]+"'");
			}
			
			if (!appUser.getPassword().equals(up[1])) {
				throw new Exception("Authorization failed for app user '"+up[0]+"'");
			}
			
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Authorized user='"+up[0]+"'");
			}
		}
		
		if (pol.isAssociateUserWithRequest()) {
			mesg.getContext().put("user", up[0]);
		}
	}

}
