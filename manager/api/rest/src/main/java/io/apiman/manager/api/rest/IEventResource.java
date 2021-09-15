package io.apiman.manager.api.rest;

import io.apiman.manager.api.beans.events.dto.NewAccountCreatedDto;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;

/**
 * An event receiving REST API.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("events")
@Api(tags = "Events")
public interface IEventResource {

    /**
     * For SSO to push new account events
     */
    @POST
    @Path("sso/users")
    @Consumes(MediaType.APPLICATION_JSON)
    void newAccountCreated(NewAccountCreatedDto newAccountCreatedDto);
}
