package io.apiman.manager.api.rest;

import com.google.common.annotations.Beta;
import io.apiman.manager.api.beans.events.dto.NewAccountCreatedDto;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * An event receiving REST API.
 *
 * <p>This is an experimental API and should not be relied upon.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("events")
@Tag(name = "Events")
@Beta
@RolesAllowed("apiuser")
public interface IEventResource {

    /**
     * For SSO to push new account events
     */
    @POST
    @Path("sso/users")
    @Consumes(MediaType.APPLICATION_JSON)
    void newAccountCreated(NewAccountCreatedDto newAccountCreatedDto);
}
