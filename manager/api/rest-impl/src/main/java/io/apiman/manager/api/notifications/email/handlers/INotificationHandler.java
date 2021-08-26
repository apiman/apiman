package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.notifications.Notification;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface INotificationHandler {
    void handle(Notification<? extends IVersionedApimanEvent> notification);
}
