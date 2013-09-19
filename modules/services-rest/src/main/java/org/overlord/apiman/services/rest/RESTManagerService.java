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

import org.overlord.apiman.model.Contract;
import org.overlord.apiman.model.Plan;
import org.overlord.apiman.model.Service;
import org.overlord.apiman.services.ManagerService;
import org.overlord.apiman.util.BeanResolverUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * This class represents the RESTful interface to the manager service.
 *
 */
@Path("/manager")
@ApplicationScoped
public class RESTManagerService {

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
    private ManagerService _managerService=null;

    /**
     * This is the default constructor.
     */
    public RESTManagerService() {
    }
    
    @PostConstruct
    public void init() {
        // Only access CDI if service not set, to support both OSGi and CDI
        if (_managerService == null) {
            _managerService = BeanResolverUtil.getBean(ManagerService.class);
        }
    }
    
    /**
     * This method sets the manager service.
     * 
     * @param ms The manager service
     */
    public void setManagerService(ManagerService ms) {
        _managerService = ms;
    }
    
    /**
     * This method returns the manager service.
     * 
     * @return The manager service
     */
    public ManagerService getManagerService() {
        return (_managerService);
    }
    
    @POST
    @Path("/service/register")
	public Response registerService(String service) throws Exception {		
        init();
        
        try {
        	Service s=MAPPER.readValue(service, Service.class);
        	
        	_managerService.registerService(s);
            
            return Response.status(Status.OK).entity("Service registered").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to register service: "+e).build();
        }
	}
	
    @GET
    @Path("/service/find")
    @Produces("application/json")
	public String getService(@QueryParam("name") String name) throws Exception {
        init();
        
    	return (MAPPER.writeValueAsString(_managerService.getService(name)));
    }
	
    @POST
    @Path("/service/update")
	public Response updateService(String service) throws Exception {		
        init();
        
        try {
        	Service s=MAPPER.readValue(service, Service.class);
        	
        	_managerService.updateService(s);
            
            return Response.status(Status.OK).entity("Service updated").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to update service: "+e).build();
        }
	}
	
    @GET
    @Path("/service/unregister")
	public Response unregisterService(@QueryParam("name") String name) throws Exception {
        init();
        
        try {
        	_managerService.unregisterService(name);
            
            return Response.status(Status.OK).entity("Service unregistered").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to unregister service: "+e).build();
        }
    }
	
    @GET
    @Path("/service/names")
    @Produces("application/json")
	public String getServiceNames() throws Exception {
        init();
        
    	return (MAPPER.writeValueAsString(_managerService.getServiceNames()));
    }
	
    @POST
    @Path("/plan/register")
	public Response registerPlan(String plan) throws Exception {		
        init();
        
        try {
        	Plan p=MAPPER.readValue(plan, Plan.class);
        	
        	_managerService.registerPlan(p);
            
            return Response.status(Status.OK).entity("Plan registered").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to register plan: "+e).build();
        }
	}
	
    @GET
    @Path("/plan/find")
    @Produces("application/json")
	public Plan getPlan(@QueryParam("id") String id) throws Exception {
        init();
        
    	return (_managerService.getPlan(id));
    }
	
    @POST
    @Path("/plan/update")
	public Response updatePlan(String plan) throws Exception {		
        init();
        
        try {
        	Plan p=MAPPER.readValue(plan, Plan.class);
        	
        	_managerService.updatePlan(p);
            
            return Response.status(Status.OK).entity("Plan updated").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to update plan: "+e).build();
        }
	}
	
    @GET
    @Path("/plan/unregister")
	public Response unregisterPlan(@QueryParam("id") String id) throws Exception {
        init();
        
        try {
        	_managerService.unregisterPlan(id);
            
            return Response.status(Status.OK).entity("Plan unregistered").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to unregister plan: "+e).build();
        }
    }
	
    @POST
    @Path("/contract/register")
	public Response registerContract(String contract) throws Exception {		
        init();
        
        try {
        	Contract c=MAPPER.readValue(contract, Contract.class);
        	
        	_managerService.registerContract(c);
            
            return Response.status(Status.OK).entity("Contract registered").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to register contract: "+e).build();
        }
	}
	
    @GET
    @Path("/contract/find")
    @Produces("application/json")
	public Contract getContract(@QueryParam("id") String id) throws Exception {
        init();
        
    	return (_managerService.getContract(id));
    }
	
    @POST
    @Path("/contract/update")
	public Response updateContract(String contract) throws Exception {		
        init();
        
        try {
        	Contract c=MAPPER.readValue(contract, Contract.class);
        	
        	_managerService.updateContract(c);
            
            return Response.status(Status.OK).entity("Contract updated").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to update contract: "+e).build();
        }
	}
	
    @GET
    @Path("/contract/unregister")
	public Response unregisterContract(@QueryParam("id") String id) throws Exception {
        init();
        
        try {
        	_managerService.unregisterContract(id);
            
            return Response.status(Status.OK).entity("Contract unregistered").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to unregister contract: "+e).build();
        }
    }
	
}
