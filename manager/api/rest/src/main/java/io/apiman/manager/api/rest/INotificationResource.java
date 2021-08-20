package io.apiman.manager.api.rest;

import io.apiman.manager.api.beans.events.dto.NewAccountCreatedDto;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import io.swagger.annotations.Api;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("notifications")
@Api("Notifications")
public interface INotificationResource {

    // TODO consider putting this somewhere else
    @POST
    @Path("system/users")
    void notifyNewAccount(NewAccountCreatedDto newAccountCreatedDto);

    // @GET
    // @Produces(MediaType.APPLICATION_JSON)
    // List<NotificationReasonBean> getNotificationReasons();
    //
    // @GET
    // @Produces(MediaType.APPLICATION_JSON)
    // List<NotificationTypeBean> getNotificationTypes();
}
