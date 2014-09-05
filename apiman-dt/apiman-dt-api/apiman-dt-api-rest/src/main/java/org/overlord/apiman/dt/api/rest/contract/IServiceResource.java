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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.overlord.apiman.dt.api.beans.policies.PolicyBean;
import org.overlord.apiman.dt.api.beans.services.ServiceBean;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.beans.summary.PolicyChainSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServicePlanSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidServiceStatusException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceVersionNotFoundException;

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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void update(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, ServiceBean bean)
            throws ServiceNotFoundException, NotAuthorizedException;

    @POST
    @Path("{organizationId}/services/{serviceId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceVersionBean createVersion(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, ServiceVersionBean bean)
            throws ServiceNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServiceVersionBean> listVersions(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId) throws ServiceNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceVersionBean getVersion(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws ServiceVersionNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{organizationId}/services/{serviceId}/versions/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateVersion(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            ServiceVersionBean bean) throws ServiceVersionNotFoundException, NotAuthorizedException,
            InvalidServiceStatusException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/plans")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServicePlanSummaryBean> getVersionPlans(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws ServiceVersionNotFoundException, NotAuthorizedException;

    @POST
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean createPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            PolicyBean bean) throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            NotAuthorizedException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean getPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies/{policyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updatePolicy(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            @PathParam("policyId") long policyId, PolicyBean bean) throws OrganizationNotFoundException,
            ServiceVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    @DELETE
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies/{policyId}")
    public void deletePolicy(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PolicyBean> listPolicies(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            NotAuthorizedException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/plans/{planId}/policyChain")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyChainSummaryBean getPolicyChain(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            @PathParam("planId") String planId) throws ServiceVersionNotFoundException,
            NotAuthorizedException;

}
