package io.apiman.manager;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/message")
@Consumes( { "application/json", "text/json" } )
@Produces( { "application/json", "text/json" } )
public class MessageRestService {

    @GET
    @Path("/{param}")
    public Response printMessage(@PathParam("param") String msg) {

        String result = "Restful example : " + msg;

        return Response.status(200).entity(result).build();

    }

}