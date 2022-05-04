package io.apiman.manager.api.beans.notifications.dto;

/**
 * Type of recipient for a notification.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public enum RecipientType {

    /**
     * Individually addressed user
     */
    INDIVIDUAL,

    /**
     * Users who hold a given role
     */
    ROLE,

    /**
     * Users who have a particular attribute
     */
    PERMISSION
}
