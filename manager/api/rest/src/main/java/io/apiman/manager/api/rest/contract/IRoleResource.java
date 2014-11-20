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

import io.apiman.manager.api.beans.idm.RoleBean;
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RoleBean create(RoleBean bean) throws RoleAlreadyExistsException, NotAuthorizedException;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<RoleBean> list() throws NotAuthorizedException;

    @GET
    @Path("{roleId}")
    @Produces(MediaType.APPLICATION_JSON)
    public RoleBean get(@PathParam("roleId") String roleId) throws RoleNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{roleId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(@PathParam("roleId") String roleId, RoleBean bean) throws RoleNotFoundException,
            NotAuthorizedException;

    @DELETE
    @Path("{roleId}")
    public void delete(@PathParam("roleId") String roleId) throws RoleNotFoundException, NotAuthorizedException;

    @POST
    @Path("search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<RoleBean> search(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException, NotAuthorizedException;

}
