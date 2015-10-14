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

import io.apiman.manager.api.beans.system.SystemStatusBean;
import io.swagger.annotations.Api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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

}
