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

import org.overlord.apiman.model.Account;
import org.overlord.apiman.model.App;
import org.overlord.apiman.model.AppUser;
import org.overlord.apiman.services.AccountService;
import org.overlord.apiman.util.AuthorizationUtil;
import org.overlord.apiman.util.BeanResolverUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
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
    public RESTAccountService() {
    }
    
    @PostConstruct
    public void init() {
        // Only access CDI if service not set, to support both OSGi and CDI
        if (_accountService == null) {
            _accountService = BeanResolverUtil.getBean(AccountService.class);
        }
    }
    
    /**
     * This method sets the account service.
     * 
     * @param as The account service
     */
    public void setAccountService(AccountService as) {
        _accountService = as;
    }
    
    /**
     * This method returns the account service.
     * 
     * @return The account service
     */
    public AccountService getAccountService() {
        return (_accountService);
    }
    
    @POST
    @Path("/user/register")
	public Response registerAccount(String account) throws Exception {
        init();
        
        try {
        	Account a=MAPPER.readValue(account, Account.class);
        	
        	_accountService.createAccount(a);
            
            return Response.status(Status.OK).entity("User account registered").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to register user account: "+e).build();
        }
	}
	
    @GET
    @Path("/user/unregister")
	public Response unregisterAccount(@QueryParam("name") String name) throws Exception {
        init();
        
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
        init();
        
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
        init();
        
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
        init();
        
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
        init();
        
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
