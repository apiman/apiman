package io.apiman.manager.api.notifications.email;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.notifications.NotificationPreferenceEntity;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.notifications.email.handlers.INotificationHandler;
import io.apiman.manager.api.service.NotificationService;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class EmailNotificationListener {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(EmailNotificationListener.class);
    private final List<INotificationHandler> handlers;
    private final NotificationService notificationService;

    @Inject
    public EmailNotificationListener(List<INotificationHandler> handlers,
         NotificationService notificationService) {
        this.handlers = handlers;
        this.notificationService = notificationService;
    }

    /**
     * Observe, filter, and process notifications.
     *
     * <ul>
     *     <li>Get email preferences for intended recipient</li>
     *     <li>Determine whether user wants emails for category notification belongs to</li>
     *     <li>If so, process notification, else drop through silently</li>
     * </ul>
     */
    public void processNotification(@Observes NotificationDto<?> notification) {
        // 1. Check who the notification should be sent to and if they even want email notifications
        notificationService.getNotificationPreference(notification.getRecipient().getId(), "email")
                           .filter(pref -> pref.getNotificationCategories().contains(notification.getCategory()))
                           .ifPresentOrElse(
                                emailPrefs -> process(notification, emailPrefs),
                                ()-> LOGGER.trace("Notification recipient did not want an email for {0}", notification)
                            );
    }

    private void process(NotificationDto<?> notification, NotificationPreferenceEntity emailPrefs) {
        LOGGER.trace("User has an email notification preference: {0}", emailPrefs);
        LOGGER.trace("Notification is a candidate for processing: {0}", notification);
        // TODO can easily optimise this for prefix matching (regex or whatever).
        handlers.stream()
             .filter(handler -> handler.wants(notification))
             .forEach(handler -> handler.handle(notification));
    }
}
