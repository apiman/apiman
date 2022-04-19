package io.apiman.manager.api.rest;

import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("apis")
@Api("Apis")
public interface IApiResource {
    /**
     * Get featured APIs across all Apiman organizations that the logged-in user has explicit or implicit permissions to access.
     *
     * <p> The identical endpoint on {@link IDeveloperPortalResource#getFeaturedApis()} serves only anonymous users
     * (and hence API Versions with {@link io.apiman.manager.api.beans.idm.DiscoverabilityLevel#PORTAL}).
     *
     * @summary Get all featured APIs across all Apiman organizations.
     * @return Array of featured APIs, else empty array if no featured APIs
     * @statuscode 200 If the action completes successfully.
     */
    @GET
    @Path("featured")
    @Produces(MediaType.APPLICATION_JSON)
    SearchResultsBean<ApiSummaryBean> getFeaturedApis();
}
