package io.apiman.manager.api.rest;

import javax.ws.rs.Path;

import io.swagger.annotations.Api;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("notifications")
@Api("Notifications")
public interface INotificationResource {

    // @GET
    // @Produces(MediaType.APPLICATION_JSON)
    // List<NotificationReasonBean> getNotificationReasons();
    //
    // @GET
    // @Produces(MediaType.APPLICATION_JSON)
    // List<NotificationTypeBean> getNotificationTypes();
}
