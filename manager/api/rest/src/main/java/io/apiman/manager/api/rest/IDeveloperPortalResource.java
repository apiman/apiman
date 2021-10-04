package io.apiman.manager.api.rest;

import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.rest.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Developer portal resources.
 * <p>
 * Many of the endpoints have more permissive access than the main API, but may provide more limited data
 * for security and privacy reasons.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("/devportal")
public interface IDeveloperPortalResource {

    @POST
    @Path("search/apis")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SearchResultsBean<ApiSummaryBean> searchApis(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException;

    @GET
    @Path("apis/featured")
    @Produces(MediaType.APPLICATION_JSON)
    List<ApiSummaryBean> getFeaturedApis();
}
