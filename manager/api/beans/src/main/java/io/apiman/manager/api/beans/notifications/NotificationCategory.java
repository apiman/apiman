package io.apiman.manager.api.beans.notifications;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public enum NotificationCategory {
    /**
     * User administration notification, such as a new account signup.
     */
    USER_ADMINISTRATION,

    /**
     * API administration notification, such as a new API signup.
     */
    API_ADMINISTRATION,

    /**
     * Some other category that isn't captured (yet).
     */
    OTHER
}
