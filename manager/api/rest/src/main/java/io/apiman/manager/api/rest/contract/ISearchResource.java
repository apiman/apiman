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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApplicationSummaryBean;
import io.apiman.manager.api.beans.summary.AvailableServiceBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.contract.exceptions.OrganizationNotFoundException;
import io.swagger.annotations.Api;

/**
 * The Search API.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("search")
@Api
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
     * Use this endpoint to search for applications.  The search criteria is
     * provided in the body of the request, including filters, order-by, and paging
     * information.
     * @summary Search for Applications
     * @param criteria The search criteria.
     * @statuscode 200 If the search is successful.
     * @return The search results (a page of applications).
     * @throws OrganizationNotFoundException when provided organization is not found
     * @throws InvalidSearchCriteriaException when provided criteria are invalid
     */
    @POST
    @Path("applications")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<ApplicationSummaryBean> searchApps(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException;

    /**
     * Use this endpoint to search for services.  The search criteria is
     * provided in the body of the request, including filters, order-by, and paging
     * information.
     * @summary Search for Services
     * @param criteria The search criteria.
     * @statuscode 200 If the search is successful.
     * @return The search results (a page of services).
     * @throws OrganizationNotFoundException when provided organization is not found
     * @throws InvalidSearchCriteriaException when provided criteria are invalid
     */
    @POST
    @Path("services")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<ServiceSummaryBean> searchServices(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException;

    /**
     * Use this endpoint to search for available services within any configured service
     * catalogs.  If no service catalogs are configured, this will always return zero
     * results.
     * @summary Search for Services in Service Catalogs
     * @param criteria The search criteria.
     * @statuscode 200 If the search is successful.
     * @return The search results (a page of available services).
     * @throws InvalidSearchCriteriaException when provided criteria are invalid
     */
    @POST
    @Path("serviceCatalogs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AvailableServiceBean> searchServiceCatalogs(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException;

}
