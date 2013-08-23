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
package org.overlord.apiman.services.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.overlord.apiman.model.Account;
import org.overlord.apiman.model.App;
import org.overlord.apiman.model.AppUser;
import org.overlord.apiman.services.AccountService;
import org.overlord.apiman.util.AuthorizationUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * This class represents the RESTful interface to the account service.
 *
 */
@Path("/account")
@ApplicationScoped
public class RESTAccountService {

    private static final Logger LOG=Logger.getLogger(RESTAccountService.class.getName());
    
    private static final ObjectMapper MAPPER=new ObjectMapper();

    static {
        SerializationConfig config=MAPPER.getSerializationConfig()
                .withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
                .withSerializationInclusion(JsonSerialize.Inclusion.NON_DEFAULT);
        
        DeserializationConfig config2=MAPPER.getDeserializationConfig()
                .without(Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        MAPPER.setSerializationConfig(config);
        MAPPER.setDeserializationConfig(config2);
    }
    
    //@javax.inject.Inject
    private AccountService _accountService=null;

    /**
     * This is the default constructor.
     */
    @SuppressWarnings("unchecked")
    public RESTAccountService() {
        
        try {
            // Need to obtain activity server directly, as inject does not
            // work for REST service, and RESTeasy/CDI integration did not
            // appear to work in AS7. Directly accessing the bean manager
            // should be portable.
            BeanManager bm=InitialContext.doLookup("java:comp/BeanManager");
            
            java.util.Set<Bean<?>> beans=bm.getBeans(AccountService.class);
            
            for (Bean<?> b : beans) {                
                CreationalContext<Object> cc=new CreationalContext<Object>() {
                    public void push(Object arg0) {
                    }
                    public void release() {
                    }                   
                };
                
                _accountService = (AccountService)((Bean<Object>)b).create(cc);
                
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Account service="+_accountService+" for bean="+b);
                }
                
                if (_accountService != null) {
                    break;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to init mnager service", e);
        }
    }
    
    @POST
    @Path("/user/register")
	public Response registerAccount(String account) throws Exception {		
        try {
        	Account a=MAPPER.readValue(account, Account.class);
        	
        	_accountService.createAccount(a);
            
            return Response.status(Status.OK).entity("User account registered").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to register user account: "+e).build();
        }
	}
	
    @GET
    @Path("/user/unregister")
	public Response unregisterAccount(@QueryParam("name") String name) throws Exception {
        try {
        	_accountService.removeAccount(name);
            
            return Response.status(Status.OK).entity("User account unregistered").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to unregister user account: "+e).build();
        }
    }
	
    @POST
    @Path("/app/register")
	public Response registerApp(String app, @Context HttpHeaders headers) throws Exception {		
        try {
        	App a=MAPPER.readValue(app, App.class);
        	
        	// Validate user
        	java.util.List<String> values=headers.getRequestHeader("Authorization");
        	
        	if (values == null || values.size() != 1) {
        		throw new Exception("No authorization header provided");
        	}
        	
        	String[] auth=AuthorizationUtil.getBasicAuthUsernamePassword(values.get(0));
        	
        	_accountService.createApp(auth[0], a);
            
            return Response.status(Status.OK).entity("App registered").build();
        } catch (Exception e) {
        	e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to register app: "+e).build();
        }
	}	
	
    @POST
    @Path("/app/update")
	public Response updateApp(String app, @Context HttpHeaders headers) throws Exception {		
        try {
        	App a=MAPPER.readValue(app, App.class);
        	
        	// Validate user
        	java.util.List<String> values=headers.getRequestHeader("Authorization");
        	
        	if (values == null || values.size() != 1) {
        		throw new Exception("No authorization header provided");
        	}
        	
        	String[] auth=AuthorizationUtil.getBasicAuthUsernamePassword(values.get(0));
        	
        	_accountService.updateApp(auth[0], a);
            
            return Response.status(Status.OK).entity("App updated").build();
        } catch (Exception e) {
        	e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to update app: "+e).build();
        }
	}
    
    @GET
    @Path("/app/unregister")
	public Response unregisterApp(@QueryParam("id") String id, @Context HttpHeaders headers) throws Exception {		
        try {
        	
        	//_accountService.removeApp(username, a);
            
            return Response.status(Status.OK).entity("App unregistered").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to unregister app: "+e).build();
        }
	}
	
    @POST
    @Path("/appuser/register")
	public Response registerAppUser(String appuser, @Context HttpHeaders headers) throws Exception {		
        try {
        	AppUser a=MAPPER.readValue(appuser, AppUser.class);
        	
        	// Validate user
        	java.util.List<String> values=headers.getRequestHeader("Authorization");
        	
        	if (values == null || values.size() != 1) {
        		throw new Exception("No authorization header provided");
        	}
        	
        	String[] auth=AuthorizationUtil.getBasicAuthUsernamePassword(values.get(0));
        	
        	_accountService.createAppUser(auth[0], a);
            
            return Response.status(Status.OK).entity("App user registered").build();
        } catch (Exception e) {
        	e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to register app user: "+e).build();
        }
	}
	
}
