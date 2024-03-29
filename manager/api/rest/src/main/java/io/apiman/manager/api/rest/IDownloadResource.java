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

import io.apiman.manager.api.rest.exceptions.DownloadNotFoundException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A simple Download API.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("downloads")
@Tag(name = "Downloads")
public interface IDownloadResource {

    /**
     * This endpoint is used to download a file that was previously generated by
     * some other REST API call.  For example, when exporting data via the
     * /system/export endpoint, a temporary download link may be created.  This
     * represents that temporary download link.  In this example, the download
     * will result in the exported data.
     * @summary Download File
     * @return A binary file.
     */
    @GET
    @Path("{downloadId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "On success.")
    })
    public Response download(@PathParam("downloadId") String downloadId) throws DownloadNotFoundException;

}
