/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.overlord.apiman.dt.api.rest.contract;

import java.util.List;

import javax.management.ServiceNotFoundException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.beans.services.ServiceBean;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceAlreadyExistsException;

/**
 * The Service API.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("organizations")
public interface IServiceResource {
    
    @POST
    @Path("{organizationId}/services")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceBean create(@PathParam("organizationId") String organizationId, ServiceBean bean)
            throws OrganizationNotFoundException, ServiceAlreadyExistsException, NotAuthorizedException;
    
    @GET
    @Path("{organizationId}/services/{serviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceBean get(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId) throws ServiceNotFoundException,
            NotAuthorizedException;

    @GET
    @Path("{organizationId}/services")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServiceSummaryBean> list(@PathParam("organizationId") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{organizationId}/services/{serviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void update(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, ServiceBean bean)
            throws ServiceNotFoundException, NotAuthorizedException;
    
    @POST
    @Path("{organizationId}/services/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<ServiceBean> search(@PathParam("organizationId") String organizationId,
            SearchCriteriaBean criteria) throws OrganizationNotFoundException, InvalidSearchCriteriaException;
}
