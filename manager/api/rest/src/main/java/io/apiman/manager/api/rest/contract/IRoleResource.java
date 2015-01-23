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

import io.apiman.manager.api.beans.idm.NewRoleBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.UpdateRoleBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.RoleAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.RoleNotFoundException;

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

/**
 * The Role API. Used to manage roles. Note: not used to manage users or user
 * membership in roles. This API simply provides a way to create and manage role
 * definitions. Typically this API is only available to system admins.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("roles")
public interface IRoleResource {

    /**
     * Use this endpoint to create a new apiman role.  A role consists of
     * a set of permissions granted to a user when that user is given the
     * role within the context of an organization.
     * @summary Create Role
     * @servicetag admin
     * @param bean The new role.
     * @statuscode 200 If the role is created successfully.
     * @return Full information about the created role.
     * @throws RoleAlreadyExistsException
     * @throws NotAuthorizedException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RoleBean create(NewRoleBean bean) throws RoleAlreadyExistsException, NotAuthorizedException;

    /**
     * This endpoint lists all of the roles currently defined in apiman.
     * @summary List all Roles
     * @statuscode 200 If the role list is returned successfully.
     * @return A list of roles.
     * @throws NotAuthorizedException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<RoleBean> list() throws NotAuthorizedException;

    /**
     * Use this endpoint to retrieve information about a single Role by its
     * ID.
     * @summary Get a Role by ID
     * @param roleId The role ID.
     * @statuscode 200 If the role is returned successfully.
     * @return A role.
     * @throws RoleNotFoundException
     * @throws NotAuthorizedException
     */
    @GET
    @Path("{roleId}")
    @Produces(MediaType.APPLICATION_JSON)
    public RoleBean get(@PathParam("roleId") String roleId) throws RoleNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update the information about an existing role.  The 
     * role is identified by its ID.
     * @summary Update a Role by ID
     * @servicetag admin
     * @param roleId The role ID.
     * @param bean Updated role information.
     * @statuscode 204 If the role is updated successfully.
     * @throws RoleNotFoundException
     * @throws NotAuthorizedException
     */
    @PUT
    @Path("{roleId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(@PathParam("roleId") String roleId, UpdateRoleBean bean) throws RoleNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to delete a role by its ID.
     * @summary Delete a Role by ID
     * @servicetag admin
     * @param roleId The role ID.
     * @statuscode 204 If the role is deleted.
     * @throws RoleNotFoundException
     * @throws NotAuthorizedException
     */
    @DELETE
    @Path("{roleId}")
    public void delete(@PathParam("roleId") String roleId) throws RoleNotFoundException, NotAuthorizedException;

    /**
     * This endpoint provides a way to search for roles.  The search criteria is
     * provided in the body of the request, including filters, order-by, and paging
     * information.
     * @summary Search for Roles
     * @param criteria The search criteria.
     * @statuscode 200 If the search completes successfully.
     * @return The search results (a page of roles).
     * @throws InvalidSearchCriteriaException
     * @throws NotAuthorizedException
     */
    @POST
    @Path("search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<RoleBean> search(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException, NotAuthorizedException;

}
