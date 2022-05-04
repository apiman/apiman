package io.apiman.manager.api.rest;

import io.apiman.manager.api.beans.download.BlobRef;

import java.io.IOException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

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
    @Path("{uid:.+}")
    Response getBlob(@PathParam("uid") String uid);

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Response uploadBlob(@NotNull MultipartFormDataInput multipartInput) throws IOException;
}
