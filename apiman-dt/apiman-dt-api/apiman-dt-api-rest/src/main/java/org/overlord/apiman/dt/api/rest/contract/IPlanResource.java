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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.overlord.apiman.dt.api.beans.plans.PlanBean;
import org.overlord.apiman.dt.api.beans.plans.PlanVersionBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.beans.summary.PlanSummaryBean;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanVersionNotFoundException;

/**
 * The Plan API.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("organizations")
public interface IPlanResource {
    
    @POST
    @Path("{organizationId}/plans")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PlanBean create(@PathParam("organizationId") String organizationId, PlanBean bean)
            throws OrganizationNotFoundException, PlanAlreadyExistsException, NotAuthorizedException;
    
    @GET
    @Path("{organizationId}/plans/{planId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PlanBean get(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId) throws PlanNotFoundException,
            NotAuthorizedException;

    @GET
    @Path("{organizationId}/plans")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlanSummaryBean> list(@PathParam("organizationId") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{organizationId}/plans/{planId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void update(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, PlanBean bean)
            throws PlanNotFoundException, NotAuthorizedException;

    @POST
    @Path("{organizationId}/plans/{planId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PlanVersionBean createVersion(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, PlanVersionBean bean)
            throws PlanNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/plans/{planId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlanVersionBean> listVersions(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId) throws PlanNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/plans/{planId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public PlanVersionBean getVersion(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version)
            throws PlanVersionNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{organizationId}/plans/{planId}/versions/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateVersion(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version,
            PlanVersionBean bean) throws PlanVersionNotFoundException, NotAuthorizedException;

    @POST
    @Path("{organizationId}/plans/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<PlanBean> search(@PathParam("organizationId") String organizationId,
            SearchCriteriaBean criteria) throws OrganizationNotFoundException, InvalidSearchCriteriaException;

}
