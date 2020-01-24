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

import io.apiman.manager.api.beans.system.SystemStatusBean;
import io.swagger.annotations.Api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A simple System API.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("system")
@Api
public interface ISystemResource {

    /**
     * This endpoint simply returns the status of the apiman system.  This is
     * a useful endpoint to use when testing a client's connection to the apiman
     * API Manager REST services.
     * @summary Get System Status
     * @statuscode 200 On success.
     * @return System status information.
     */
    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemStatusBean getStatus();
    
    /**
     * Use this endpoint to export data from the API Manager to a file.  All data
     * in the API Manager, including global/admin information, will be exported.
     * The resulting file should be suitable for importing into some other instance
     * of the apiman API Manager.  This is useful for upgrades, migrations between
     * environments, and backups.
     * @summary Export Data
     * @servicetag admin
     * @statuscode 200 On successful export
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @return A full export of all API Manager data
     */
    @GET
    @Path("export")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportData(@QueryParam("download") String download) throws NotAuthorizedException;
    // "Internal" method - called by the download resource.
    public Response exportData();
    
    @POST
    @Path("import")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response importData() throws NotAuthorizedException;
    
}
