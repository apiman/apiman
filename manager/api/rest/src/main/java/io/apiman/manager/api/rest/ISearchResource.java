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

package io.apiman.manager.api.rest;

import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.search.searchResults.UserSearchResult;
import io.apiman.manager.api.beans.summary.*;
import io.apiman.manager.api.rest.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.swagger.annotations.Api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * The Search API.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("search")
@Api(tags = "Search")
public interface ISearchResource {

    /**
     * Use this endpoint to search for organizations.  The search criteria is
     * provided in the body of the request, including filters, order-by, and paging
     * information.
     * @summary Search for Organizations
     * @param criteria The search criteria.
     * @statuscode 200 If the search is successful.
     * @return The search results (a page of organizations).
     * @throws InvalidSearchCriteriaException when provided criteria are invalid when provided criteria are invalid
     */
    @POST
    @Path("organizations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<OrganizationSummaryBean> searchOrgs(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException;

    /**
     * Use this endpoint to search for clients.  The search criteria is
     * provided in the body of the request, including filters, order-by, and paging
     * information.
     * @summary Search for Clients
     * @servicetag admin
     * @param criteria The search criteria.
     * @statuscode 200 If the search is successful.
     * @return The search results (a page of clients).
     * @throws OrganizationNotFoundException when provided organization is not found
     * @throws InvalidSearchCriteriaException when provided criteria are invalid
     * @throws NotAuthorizedException when not authorized to invoke this method
     */
    @POST
    @Path("clients")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<ClientSummaryBean> searchClients(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException, NotAuthorizedException;

    /**
     * Use this endpoint to search for APIs.  The search criteria is
     * provided in the body of the request, including filters, order-by, and paging
     * information.
     * @summary Search for APIs
     * @param criteria The search criteria.
     * @statuscode 200 If the search is successful.
     * @return The search results (a page of APIs).
     * @throws OrganizationNotFoundException when provided organization is not found
     * @throws InvalidSearchCriteriaException when provided criteria are invalid
     */
    @POST
    @Path("apis")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<ApiSummaryBean> searchApis(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException;

    /**
     * Use this endpoint to search for available APIs within any configured API
     * catalogs.  If no API catalogs are configured, this will always return zero
     * results.
     * @summary Search for APIs in API Catalogs
     * @param criteria The search criteria.
     * @statuscode 200 If the search is successful.
     * @return The search results (a page of available APIs).
     * @throws InvalidSearchCriteriaException when provided criteria are invalid
     */
    @POST
    @Path("apiCatalog/entries")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AvailableApiBean> searchApiCatalog(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException;

    /**
     * Use this endpoint to get a list of all namespaces available to be searched
     * within.  Not all platforms support this functionality.  If no namespaces are
     * found, then the UI should simply suppress the namespace filter.
     * @summary List All Namespaces in API Catalogs
     * @statuscode 200 If the namespaces were successfully returned.
     * @return The list of namespaces.
     */
    @GET
    @Path("apiCatalog/namespaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiNamespaceBean> getApiNamespaces();

    /**
     * Use this endpoint to search for users.  The search criteria is
     * provided in the body of the request, including filters, order-by, and paging
     * information.
     *
     * @param criteria The search criteria.
     * @return The search results (a page of users).
     * @throws InvalidSearchCriteriaException when provided criteria are invalid
     * @summary Search for Users
     * @statuscode 200 If the search is successful.
     */
    @POST
    @Path("users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SearchResultsBean<UserSearchResult> searchUsers(SearchCriteriaBean criteria) throws InvalidSearchCriteriaException;

    /**
     * This endpoint provides a way to search for roles.  The search criteria is
     * provided in the body of the request, including filters, order-by, and paging
     * information.@
     * @summary Search for Roles
     * @param criteria The search criteria.
     * @statuscode 200 If the search completes successfully.
     * @return The search results (a page of roles).
     * @throws InvalidSearchCriteriaException when provided criteria are invalid
     */
    @POST
    @Path("roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<RoleBean> searchRoles(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException;

}
