package io.apiman.manager.api.rest;

import io.apiman.manager.api.beans.events.dto.NewAccountCreatedDto;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("events")
public interface IEventResource {

    @POST
    @Path("sso/users")
    void notifyNewAccount(NewAccountCreatedDto newAccountCreatedDto);
}
