package io.apiman.manager.api.notifications;

/**
 * Something that receives and processes a notification.
 *
 * <p>And hopefully does something useful with it!
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface INotificationProcessor {

    /**
     * Process a new notification.
     */
    void processNotification(Notification<?> notification);

}
