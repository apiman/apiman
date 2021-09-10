package io.apiman.manager.api.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("blobs")
@Api(tags = "Blobs")
public interface IBlobResource {

    @GET
    @Path("{uid}")
    Response getBlob(@PathParam("uid") String uid);
}
