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

package io.apiman.gateway.api.rest;

import io.apiman.gateway.api.rest.exceptions.NotAuthorizedException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

/**
 * Org API
 */
@Path("/")
@io.swagger.annotations.Api
public interface IOrgResource {

    // Organisation
    /**
     * Paginated list of Org names.
     *
     * @param response List\<String\> of Org names.
     *
     * @throws NotAuthorizedException when unauthorized
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("organizations")
    public void listOrgs(@Suspended final AsyncResponse response) throws NotAuthorizedException;
}
