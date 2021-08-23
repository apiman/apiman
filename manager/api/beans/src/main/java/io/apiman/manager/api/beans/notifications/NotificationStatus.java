package io.apiman.manager.api.beans.notifications;

/**
 * Status of a notification.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public enum NotificationStatus {
    /**
     * The notification is open.
     *
     * <p>This does not guarantee that nobody has seen the notification, as certain unidirectional notifications
     * (e.g. SMS, email) may not provide a guaranteed method to indicate that someone has read it.
     */
    OPEN,

    /**
     * User has explicitly dismissed a notification.
     */
    USER_DISMISSED,

    /**
     * The system has dismissed a notification without direct user action. For example, it is old and has been timed
     * out, or something has indicated that the user has read it (e.g. URL with click-through tracking).
     */
    SYSTEM_DISMISSED
}
