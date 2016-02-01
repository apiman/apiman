package io.apiman.manager.api.rest.contract;

/**
 * Created by e050764 on 1/20/16.
 */
import io.apiman.manager.api.rest.contract.exceptions.InvalidApiStatusException;


import javax.ws.rs.Path;
import javax.ws.rs.DELETE;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;

@Path("api")
@Api
public interface IAPIResource {

    /**
     * Delete an unpublished API.
     */
    @DELETE
    @Path("unpublished/{orgId}/{apiId}")
    public void delete(@PathParam("orgId") String orgId, @PathParam("apiId") String serviceId) throws InvalidApiStatusException;

    /*
        TODO:
        We can give an additional ability to delete a specific version of an API that is unpublished.
        PUT /api/{orgId}/{apiId}
        {"name": ""}
     */
}