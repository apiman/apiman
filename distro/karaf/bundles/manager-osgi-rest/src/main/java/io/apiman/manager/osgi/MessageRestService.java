package io.apiman.manager.osgi;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/message")
@Consumes( { "application/json", "text/json" } )
@Produces( { "application/json", "text/json" } )
@ApplicationScoped
public class MessageRestService {

    @GET
    @Path("/{param}")
    public Response printMessage(@PathParam("param") String msg) {

        String result = "Restful example : " + msg;

        return Response.status(200).entity(result).build();

    }

}