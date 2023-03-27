package io.apiman.manager.api.rest;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Serve blobs via REST.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("blobs")
@Tag(name = "Blobs")
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
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "If your upload has been accepted")
    })
    Response uploadBlob(@NotNull MultipartFormDataInput multipartInput) throws IOException;
}
