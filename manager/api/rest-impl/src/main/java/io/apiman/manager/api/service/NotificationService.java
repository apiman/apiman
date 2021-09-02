package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.JsonUtil;
import io.apiman.common.util.Preconditions;
import io.apiman.manager.api.beans.notifications.NotificationEntity;
import io.apiman.manager.api.beans.notifications.NotificationStatus;
import io.apiman.manager.api.beans.notifications.dto.CreateNotificationDto;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.beans.notifications.dto.RecipientDto;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.INotificationRepository;
import io.apiman.manager.api.notifications.mappers.NotificationMapper;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Notifications for tell users useful things. Once a notification has been created with {@link
 * #sendNotification(CreateNotificationDto)}, the event is fired through CDI's notification system. These are then
 * caught by various handlers (such as email), which will do something sensible with emails they know how to handle.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class NotificationService implements DataAccessUtilMixin {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(NotificationService.class);

    private INotificationRepository notificationRepository;
    private NotificationMapper notificationMapper;
    private Event<NotificationDto<?>> notificationDispatcher;

    @Inject
    public NotificationService(
         INotificationRepository notificationRepository,
         NotificationMapper notificationMapper,
         Event<NotificationDto<?>> notificationDispatcher,
         ISecurityContext securityContext) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.notificationDispatcher = notificationDispatcher;
    }

    public NotificationService() {

    }

    public int unreadNotifications(String userId) {
        return notificationRepository.countUnreadNotificationsByUserId(userId);
    }

    /**
     * Get the latest notifications for a given user/recipient.
     *
     * @param recipientId intended recipient of the notification.
     * @param paging pagination.
     * @return results with list of NotificationEntity and paging info.
     */
    public SearchResultsBean<NotificationEntity> getLatestNotifications(String recipientId, PagingBean paging) {
        return tryAction(() -> notificationRepository.getUnreadNotificationsByRecipientId(recipientId, paging));
    }

    /**
     * Send a new notification to a specified recipient (userId)
     *
     * @param newNotification the new notification.
     */
    public void sendNotification(CreateNotificationDto newNotification) {
        LOGGER.debug("Creating new notification(s): {0}", newNotification);

        // Can have multiple recipients for same notification.
        for (RecipientDto recipientDto : newNotification.getRecipient()) {
            // In DB we store this using a pattern
            //String recipient = calculateRecipient(recipientDto);

            NotificationEntity notificationEntity = new NotificationEntity()
                 .setCategory(newNotification.getCategory())
                 .setReason(newNotification.getReason())
                 .setReasonMessage(newNotification.getReasonMessage())
                 .setRecipient(recipient)
                 .setSource(newNotification.getSource())
                 .setPayload(JsonUtil.toJsonTree(newNotification.getPayload()));

            NotificationDto<?> dto = notificationMapper.entityToDto(notificationEntity);

            tryAction(() -> {
                // 1. Save notification into notifications table.
                LOGGER.trace("Creating notification entity in repository layer: {0}", notificationEntity);
                notificationRepository.create(notificationEntity);
                // 2. Emit notification onto notification bus.
                LOGGER.trace("Firing notification event: {0}", dto);
                notificationDispatcher.fire(dto);
            });
        }
    }

    /**
     * Mark a list of notifications as read. They must be owned by the same recipient.
     *
     * <p>Any attempt by a user to mark the notifications that do not actually belong to them will result in
     * nothing happening (i.e. silently failing in some way).
     *
     * <p>You may want to take the userId from the security context when executing on behalf of an external entity to
     * ensure that the user is actually who they claim to be.
     *
     * @param recipientId     ID of the owner/recipient of the notification. We need this to prevent unauthorised users
     *                        interfering with other users' notifications.
     * @param notificationIds list of notification IDs
     *
     * @param status          status to set (e.g. system read, user read)
     */
    public void markNotificationsAsRead(String recipientId, List<Long> notificationIds, NotificationStatus status) {
        if (notificationIds.isEmpty())
            return;

        Preconditions.checkArgument(status != NotificationStatus.OPEN,
             "When marking a notification as read a non-OPEN status must be provided: " + status);

        LOGGER.trace("Marking recipient {0} notifications {1} as read {2}", recipientId, notificationIds, status);

        tryAction(() -> notificationRepository.markNotificationsReadById(recipientId, notificationIds, status));
    }

    private String calculateRecipient(RecipientDto recipientDto) {
        switch (recipientDto.getRecipientType()) {
            case INDIVIDUAL:
                return recipientDto.getRecipient();
            case ROLE:
                return "role/" + recipientDto.getRecipient();
            case ATTRIBUTE:
                return "attribute/" + recipientDto.getRecipient();
            default:
                throw new IllegalStateException("Unexpected value: " + recipientDto.getRecipientType());
        }
    }

}
