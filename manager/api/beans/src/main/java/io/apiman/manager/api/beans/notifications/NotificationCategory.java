package io.apiman.manager.api.beans.notifications;

/**
 * Coarse-grained notification category (useful for simple filtering and subscription management)
 *
 * Add categories as we go...
 *
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
     * Something relating to the API lifecycle (register, etc).
     */
    API_LIFECYCLE,

    /**
     * Some other category that isn't captured (yet).
     */
    OTHER
}
