package io.apiman.manager.api.notifications.email.reasonhandlers;

import io.apiman.manager.api.notifications.Notification;

import java.util.Map;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class AccountSignupApproval implements IReasonHandler {

    public AccountSignupApproval() {
    }

    @Override
    public Map<String, Object> handle(Notification<?> notification) {
        Map.of("");
    }
}
