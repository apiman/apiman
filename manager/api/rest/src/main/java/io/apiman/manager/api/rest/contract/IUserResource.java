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

package io.apiman.manager.api.rest.contract;

import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApplicationSummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.UserNotFoundException;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * The User API.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("users")
public interface IUserResource {

    @GET
    @Path("{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserBean get(@PathParam("userId") String userId) throws UserNotFoundException;

    @PUT
    @Path("{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(@PathParam("userId") String userId, UserBean user) throws UserNotFoundException, NotAuthorizedException;

    @POST
    @Path("search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<UserBean> search(SearchCriteriaBean criteria) throws InvalidSearchCriteriaException;

    @GET
    @Path("{userId}/organizations")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OrganizationSummaryBean> getOrganizations(@PathParam("userId") String userId);

    @GET
    @Path("{userId}/applications")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApplicationSummaryBean> getApplications(@PathParam("userId") String userId);

    @GET
    @Path("{userId}/services")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServiceSummaryBean> getServices(@PathParam("userId") String userId);

    @GET
    @Path("{userId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getActivity(@PathParam("userId") String userId,
            @QueryParam("page") int page, @QueryParam("count") int pageSize);
    
}
