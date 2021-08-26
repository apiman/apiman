package io.apiman.manager.api.notifications;

import io.apiman.manager.api.beans.events.IVersionedApimanEvent;

/**
 * Something that receives and processes a notification.
 * And hopefully does something useful with it!
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface INotificationProducer {

    /**
     * Process a new notification.
     */
    void processEvent(IVersionedApimanEvent notification);

    /**
     *
     */
    // boolean isInterestedIn(String type);
}
