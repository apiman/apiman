package io.apiman.manager.api.notifications.email.reasonhandlers;

import io.apiman.manager.api.notifications.Notification;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface IReasonHandler {
    void handle(Notification<?> notification);
}
