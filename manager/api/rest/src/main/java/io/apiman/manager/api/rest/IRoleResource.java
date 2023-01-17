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

import io.apiman.manager.api.beans.idm.NewRoleBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.UpdateRoleBean;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.RoleAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.RoleNotFoundException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * The Role API. Used to manage roles. Note: not used to manage users or user
 * membership in roles. This API simply provides a way to create and manage role
 * definitions. Typically, this API is only available to system admins.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("roles")
@Tag(name = "Roles")
@PermitAll
public interface IRoleResource {

    /**
     * Use this endpoint to create a new apiman role.  A role consists of
     * a set of permissions granted to a user when that user is given the
     * role within the context of an organization.
     * @summary Create Role
     * @servicetag admin
     * @param bean The new role.
     * @return Full information about the created role.
     * @throws RoleAlreadyExistsException when role already exists
     * @throws NotAuthorizedException when not authorized to invoke this method
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the role is created successfully.")
    })
    public RoleBean create(NewRoleBean bean) throws RoleAlreadyExistsException, NotAuthorizedException;

    /**
     * This endpoint lists all of the roles currently defined in apiman.
     * @summary List all Roles
     * @servicetag admin
     * @return A list of roles.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the role list is returned successfully.")
    })
    public List<RoleBean> list();

    /**
     * Use this endpoint to retrieve information about a single Role by its
     * ID.
     * @summary Get a Role by ID
     * @servicetag admin
     * @param roleId The role ID.
     * @return A role.
     * @throws RoleNotFoundException when a request is sent for a role that does not exist
     */
    @GET
    @Path("{roleId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the role is returned successfully.")
    })
    public RoleBean get(@PathParam("roleId") String roleId) throws RoleNotFoundException;

    /**
     * Use this endpoint to update the information about an existing role.  The
     * role is identified by its ID.
     * @summary Update a Role by ID
     * @servicetag admin
     * @param roleId The role ID.
     * @param bean Updated role information.
     * @throws RoleNotFoundException when a request is sent for a role that does not exist
     * @throws NotAuthorizedException when not authorized to invoke this method
     */
    @PUT
    @Path("{roleId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the role is updated successfully.")
    })
    public void update(@PathParam("roleId") String roleId, UpdateRoleBean bean) throws RoleNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to delete a role by its ID.
     * @summary Delete a Role by ID
     * @servicetag admin
     * @param roleId The role ID.
     * @throws RoleNotFoundException when a request is sent for a role that does not exist
     * @throws NotAuthorizedException when not authorized to invoke this method
     */
    @DELETE
    @Path("{roleId}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the role is deleted.")
    })
    public void delete(@PathParam("roleId") String roleId) throws RoleNotFoundException, NotAuthorizedException;
}
