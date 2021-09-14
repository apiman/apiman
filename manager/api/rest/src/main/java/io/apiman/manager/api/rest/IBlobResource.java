package io.apiman.manager.api.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Serve blobs via REST.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("blobs")
@Api(tags = "Blobs")
public interface IBlobResource {

    /**
     * Get a blob. The Content-Type header is set.
     *
     * @param uid the file's UID
     * @return the file in the response
     */
    @GET
    @Path("{uid}")
    Response getBlob(@PathParam("uid") String uid);
}
