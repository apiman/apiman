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
package org.overlord.apiman.model.policies;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.overlord.apiman.Message;
import org.overlord.apiman.model.AppUser;
import org.overlord.apiman.model.Policy;
import org.overlord.apiman.policy.PolicyContext;
import org.overlord.apiman.util.AuthorizationUtil;

public class BasicAuthPolicy extends Policy {
    
    private static final Logger LOG=Logger.getLogger(BasicAuthPolicy.class.getName());

	private boolean _authenticate=true;
	private boolean _associateUserWithRequest=true;
	
	/**
	 * This method determines if the request should be authenticated
	 * or passed through to the service for authentication.
	 * 
	 * @return Whether to authenticate the request in the gateway
	 */
	public boolean isAuthenticate() {
		return (_authenticate);
	}
	
	/**
	 * This method sets whether the request should be authenticated
	 * or passed through to the service for authentication.
	 * 
	 * @param b Whether to authenticate the request in the gateway
	 */
	public void setAuthenticate(boolean b) {
		_authenticate = b;
	}
	
	/**
	 * This method determines whether the user should be associated
	 * with the request.
	 * 
	 * @return Whether to associate the user with the request
	 */
	public boolean isAssociateUserWithRequest() {
		return (_associateUserWithRequest);
	}
	
	/**
	 * This method determines whether the user should be associated
	 * with the request.
	 * 
	 * @param b Whether to associate the user with the request
	 */
	public void setAssociateUserWithRequest(boolean b) {
		_associateUserWithRequest = b;
	}
	
    /**
     * {@inheritDoc}
     */
    public void apply(PolicyContext context, Message mesg) throws Exception {    
        
        String auth=(String)mesg.getHeader("Authorization");
        
        if (auth == null) {
            throw new Exception("No authorization information available");
        }
        
        String[] up=AuthorizationUtil.getBasicAuthUsernamePassword(auth);
        
        if (isAuthenticate()) {
            
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
        
        if (isAssociateUserWithRequest()) {
            mesg.getContext().put("user", up[0]);
        }
    }
}
