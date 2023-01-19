package io.apiman.manager.api.rest;

import com.google.common.annotations.Beta;
import io.apiman.manager.api.beans.apis.dto.ApiVersionBeanDto;
import io.apiman.manager.api.beans.developers.ApiVersionPolicySummaryDto;
import io.apiman.manager.api.beans.developers.DeveloperApiPlanSummaryDto;
import io.apiman.manager.api.beans.orgs.NewOrganizationBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionEndpointSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.rest.exceptions.ApiVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.exceptions.InvalidApiStatusException;
import io.apiman.manager.api.rest.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.PolicyNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Developer portal resources.
 * <p>
 * Many of the endpoints have more permissive access than the main API, but may provide more limited data
 * for security and privacy reasons.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("devportal")
@Tags({ @Tag(name = "Devportal"), @Tag(name = "Experimental") })
@Beta
public interface IDeveloperPortalResource {
    @POST
    @Path("search/apis")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search Apiman APIs")
    SearchResultsBean<ApiSummaryBean> searchApis(@RequestBody SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException;

    @GET
    @Path("apis/featured")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all featured APIs")
    SearchResultsBean<ApiSummaryBean> getFeaturedApis();

    @POST
    @Path("organizations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create home org for developer",
            description = "Create a 'home' organization on behalf of the portal user (they may not normally have permissions to do this themselves)."
    )
    Response createHomeOrgForDeveloper(@RequestBody NewOrganizationBean newOrg)
            throws OrganizationAlreadyExistsException;

    @GET
    @Path("organizations/{orgId}/apis/{apiId}/versions/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all API Versions within an organization")
    List<ApiVersionSummaryBean> listApiVersions(
            @PathParam("orgId") @Parameter(description = "The Organization ID") String orgId,
            @PathParam("apiId") @Parameter(description = "The API ID") String apiId
    );

    @GET
    @Path("organizations/{orgId}/apis/{apiId}/versions/{apiVersion}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a specific API Version")
    ApiVersionBeanDto getApiVersion(
            @PathParam("orgId") @Parameter(description = "The Organization ID") String orgId,
            @PathParam("apiId") @Parameter(description = "The API ID") String apiId,
            @PathParam("apiVersion") @Parameter(description = "The API Version") String apiVersion
    ) throws ApiVersionNotFoundException;

    @GET
    @Path("organizations/{orgId}/apis/{apiId}/versions/{apiVersion}/plans")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all Plans for an API Version")
    List<DeveloperApiPlanSummaryDto> getApiVersionPlans(
            @PathParam("orgId") @Parameter(description = "The Organization ID") String orgId,
            @PathParam("apiId") @Parameter(description = "The API ID") String apiId,
            @PathParam("apiVersion") @Parameter(description = "The API Version") String apiVersion
    ) throws ApiVersionNotFoundException;

    @GET
    @Path("organizations/{orgId}/apis/{apiId}/versions/{apiVersion}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all policies on an API Version")
    List<ApiVersionPolicySummaryDto> listApiPolicies(
            @PathParam("orgId") @Parameter(description = "The Organization ID") String orgId,
            @PathParam("apiId") @Parameter(description = "The API ID") String apiId,
            @PathParam("apiVersion") @Parameter(description = "The API Version") String apiVersion
    ) throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException;

    @GET
    @Path("organizations/{orgId}/apis/{apiId}/versions/{apiVersion}/definition")
    @Produces({ MediaType.APPLICATION_JSON, "application/wsdl+xml", "application/x-yaml" })
    @Operation(summary = "Get an API Definition (schema) for an API Version")
    Response getApiDefinition(
            @PathParam("orgId") @Parameter(description = "The Organization ID") String orgId,
            @PathParam("apiId") @Parameter(description = "The API ID") String apiId,
            @PathParam("apiVersion") @Parameter(description = "The API Version") String apiVersion
    ) throws ApiVersionNotFoundException;

    @GET
    @Path("organizations/{orgId}/apis/{apiId}/versions/{apiVersion}/endpoint")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get endpoint information for an API Version")
    ApiVersionEndpointSummaryBean getApiVersionEndpointInfo(
            @PathParam("orgId") @Parameter(description = "The Organization ID") String orgId,
            @PathParam("apiId") @Parameter(description = "The API ID") String apiId,
            @PathParam("apiVersion") @Parameter(description = "The API Version") String apiVersion
    ) throws ApiVersionNotFoundException, InvalidApiStatusException, GatewayNotFoundException;

    @GET
    @Path("organizations/{orgId}/plans/{planId}/versions/{planVersion}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all policies on a specific Plan Version")
    List<PolicySummaryBean> listPlanPolicies(
            @PathParam("orgId") @Parameter(description = "The Organization ID") String organizationId,
            @PathParam("planId") @Parameter(description = "The Plan ID") String planId,
            @PathParam("planVersion") @Parameter(description = "The Plan Version") String planVersion
    ) throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException;

    @GET
    @Path("organizations/{orgId}/plans/{planId}/versions/{planVersion}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a specific policy on a plan version")
    PolicyBean getPlanPolicy(
            @PathParam("orgId") @Parameter(description = "The Organization ID") String organizationId,
            @PathParam("planId") @Parameter(description = "The Plan ID") String planId,
            @PathParam("planVersion") @Parameter(description = "The Plan Version") String planVersion,
            @PathParam("policyId") @Parameter(description = "The Policy ID") long policyId
    ) throws OrganizationNotFoundException, PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

}
