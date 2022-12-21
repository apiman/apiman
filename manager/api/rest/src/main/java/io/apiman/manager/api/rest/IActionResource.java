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

import io.apiman.manager.api.beans.actions.ActionBean;
import io.apiman.manager.api.beans.actions.ContractActionDto;
import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.rest.exceptions.ActionException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * The Action API.  This API allows callers to perform actions on various
 * entities - actions other than the standard REST "crud" actions.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("actions")
@Tag(name = "Actions")
public interface IActionResource {

    /**
     * Call this endpoint in order to execute actions for apiman entities such
     * as Plans, APIs, or Clients.  The type of the action must be
     * included in the request payload.
     * @param action The details about what action to execute.
     * @throws ActionException action is performed but an error occurs during processing
     */
    @PermitAll
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the action completes successfully.")
    })
    @Operation(summary = "Execute an Entity Action")
    void performAction(ActionBean action) throws ActionException, NotAuthorizedException;

    /**
     * Call this endpoint to approve a contract. If all contracts for a given {@link ClientVersionBean} have been
     * approved, then it will transition from {@link ClientStatus#AwaitingApproval} to {@link ClientStatus#Ready} and
     * hence can be published.
     * <p>
     * Requires a user with {@link PermissionType#planAdmin} permissions.
     *
     * @param action The details about what action to execute.
     * @throws ActionException action is performed but an error occurs during processing
     */
    @RolesAllowed("apiuser")
    @POST
    @Path("contracts")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the action completes successfully.")
    })
    @Operation(summary = " Approve a contract")
    void approveContract(ContractActionDto action) throws ActionException, NotAuthorizedException;
}
