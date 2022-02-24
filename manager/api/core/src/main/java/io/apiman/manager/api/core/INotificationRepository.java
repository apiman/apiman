package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.notifications.NotificationEntity;
import io.apiman.manager.api.beans.notifications.NotificationPreferenceEntity;
import io.apiman.manager.api.beans.notifications.NotificationStatus;
import io.apiman.manager.api.beans.notifications.NotificationType;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.util.List;
import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ParametersAreNonnullByDefault
public interface INotificationRepository {

    /**
     * Get notification by notification's unique ID
     */
    NotificationEntity getNotificationById(Long notificationId) throws StorageException;

    /**
     * Search for notifications by recipient using search criteria
     */
    SearchResultsBean<NotificationEntity> searchNotificationsByUser(String recipientUserId,
         @Nullable SearchCriteriaBean searchCriteria)
         throws StorageException;

    /**
     * Get latest notifications for a recipient
     */
    SearchResultsBean<NotificationEntity> getLatestNotificationsByRecipientId(String recipientUserId, @Nullable PagingBean pagingBean)
         throws StorageException;

    /**
     * Create a new notifications
     */
    void create(NotificationEntity bean) throws StorageException;

    /**
     * Update a notifications
     */
    void update(NotificationEntity bean) throws StorageException;

    /**
     * Delete a notification
     */
    void delete(NotificationEntity bean) throws StorageException;

    /**
     * Delete a notification by ID
     */
    void deleteById(Long id) throws StorageException;

    /**
     * Delete all notifications. Ensure you truly want to delete and not just mark as read!)
     */
    void deleteAll();

    /**
     * Delete all notifications by recipient ID. Ensure you truly want to delete and not just mark as read!)
     * @param recipientUserId the recipient
     */
    void deleteByUserId(String recipientUserId);

    /**
     * Count the number of notifications by user with the given status(es)
     */
    int countNotificationsByUserId(String recipientUserId, List<NotificationStatus> notificationStatus);

    /**
     * Mark the notifications with the given IDs with the provided status.
     *
     * If a notification ID does not belong to recipientUserId, it will be silently ignored.
     */
    void markNotificationsWithStatusById(String recipientUserId, List<Long> idList, NotificationStatus status) throws StorageException;

    /**
     * Mark all OPEN notifications as read for the given recipient.
     *
     * If a notification ID does not belong to recipientUserId, it will be silently ignored.
     */
    void markAllNotificationsReadByUserId(String recipientUserId, NotificationStatus status);

    /**
     * Get a user's notification preferences
     */
    Optional<NotificationPreferenceEntity> getNotificationPreferenceByUserIdAndType(String userId, NotificationType notificationType);
}
