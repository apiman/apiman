package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.JsonUtil;
import io.apiman.manager.api.beans.notifications.NotificationEntity;
import io.apiman.manager.api.core.INotificationRepository;
import io.apiman.manager.api.notifications.Notification;
import io.apiman.manager.api.notifications.dto.CreateNotificationDto;
import io.apiman.manager.api.notifications.dto.RecipientType;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;

import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class NotificationService implements DataAccessUtilMixin {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(NotificationService.class);

    private INotificationRepository notificationRepository;
    private Event<Notification<?>> notificationDispatcher;

    @Inject
    public NotificationService(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationService() {
    }

    public List<Notification<?>> getLatestNotificationsForUser(String userId) {
        return Collections.emptyList();
    }

    public void sendNotification(CreateNotificationDto newNotification) {
        LOGGER.debug("Creating new notification: {0}", newNotification);

        String recipient = calculateRecipient(newNotification);

        NotificationEntity notificationEntity = new NotificationEntity()
             .setCategory(newNotification.getCategory())
             .setReason(newNotification.getReason())
             .setReasonMessage(newNotification.getReasonMessage())
             .setRecipient(recipient)
             .setSource(newNotification.getSource())
             .setPayload(JsonUtil.toJsonTree(newNotification.getPayload()));

        tryAction(() -> {
            // 1. Save notification into notifications table.
            notificationRepository.create(notificationEntity);

            // 2. Emit notification onto
            notificationDispatcher.fire();
        });
    }

    private String calculateRecipient(CreateNotificationDto newNotification) {
        if (newNotification.getRecipientType() == RecipientType.INDIVIDUAL) {
            return newNotification.getRecipient();
        } else if (newNotification.getRecipientType() == RecipientType.ROLE) {
            return "roles/" + newNotification.getRecipient();
        } else {
            throw new IllegalArgumentException("Don't know how to handle " + newNotification.getRecipientType());
        }
    }

    //public List get

}
