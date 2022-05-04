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
     * Something relating to the API lifecycle (publish, retire, etc).
     */
    API_LIFECYCLE,

    /**
     * Something relating to the Client App lifecycle (register, unregister, etc).
     */
    CLIENT_LIFECYCLE,

    /**
     * A system action (including certain technical events).
     */
    SYSTEM,

    /**
     * Some other category that isn't captured (yet).
     */
    OTHER
}
