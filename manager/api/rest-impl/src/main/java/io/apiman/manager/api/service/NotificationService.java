package io.apiman.manager.api.service;

import io.apiman.manager.api.core.INotificationRepository;
import io.apiman.manager.api.notifications.Notification;

import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class NotificationService {

    private INotificationRepository notificationRepository;

    @Inject
    public NotificationService(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationService() {
    }

    public List<Notification<?>> getLatestNotificationsForUser(String userId) {
        return Collections.emptyList();
    }

    public List get

}
