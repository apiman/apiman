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
public class NotificationListener {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(NotificationListener.class);
    private final List<INotificationHandler> handlers;
    private final NotificationService notificationService;
    // private final Map<String, INotificationHandler> notificationProcessors = new HashMap<>();

    @Inject
    public NotificationListener(List<INotificationHandler> handlers,
         NotificationService notificationService) {
        this.handlers = handlers;
        this.notificationService = notificationService;
    }

    /**
     * {@inheritDoc}
     */
    public void processNotification(@Observes NotificationDto<?> notification) {
        // 1. Check who the notification should be sent to and if they even want email notifications
        notificationService.getNotificationPreference(notification.getRecipient(), "email")
                           .filter(pref -> pref.getNotificationCategories().contains(notification.getCategory()))
                           .ifPresent(preference -> process(notification, preference));
    }

    private void process(NotificationDto<?> notification, NotificationPreferenceEntity preference) {
        LOGGER.trace("User has an email notification preference: {0}", preference);
        LOGGER.trace("Notification is a candidate for processing: {0}", notification);
        // TODO can easily optimise this for prefix matching (regex or whatever).
        handlers.stream()
             .filter(handler -> handler.wants(notification))
             .forEach(handler -> handler.handle(notification));
    }
}
