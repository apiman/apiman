package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.notifications.Notification;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class AccountSignupApproval implements INotificationHandler {

    public AccountSignupApproval() {
    }

    @Override
    public void handle(Notification<? extends IVersionedApimanEvent> notification) {

    }

    // @Override
    // public Map<String, Object> handle(Notification<P> notification) {
    //     Map.of("");
    // }
}
