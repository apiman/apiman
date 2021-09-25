package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.notifications.NotificationEntity;
import io.apiman.manager.api.beans.notifications.NotificationPreferenceEntity;
import io.apiman.manager.api.beans.notifications.NotificationStatus;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface INotificationRepository {

    /**
     * Get notification by notification's unique ID
     */
    NotificationEntity getNotificationById(@NotNull Long notificationId) throws StorageException;

    /**
     * Search for notifications by recipient using search criteria
     */
    SearchResultsBean<NotificationEntity> searchNotificationsByUser(@NotNull String recipientUserId,
         @Nullable SearchCriteriaBean searchCriteria)
         throws StorageException;

    /**
     * Get latest notifications for a recipient
     */
    SearchResultsBean<NotificationEntity> getLatestNotificationsByRecipientId(@NotNull String recipientUserId, @Nullable PagingBean pagingBean)
         throws StorageException;

    /**
     * Create a new notifications
     */
    void create(@NotNull NotificationEntity bean) throws StorageException;

    /**
     * Update a notifications
     */
    void update(@NotNull NotificationEntity bean) throws StorageException;

    /**
     * Delete a notification
     */
    void delete(@NotNull NotificationEntity bean) throws StorageException;

    /**
     * Delete a notification by ID
     */
    void deleteById(@NotNull Long id) throws StorageException;

    /**
     * Delete all notifications. Ensure you truly want to delete and not just mark as read!)
     */
    void deleteAll();

    /**
     * Delete all notifications by recipient ID. Ensure you truly want to delete and not just mark as read!)
     * @param recipientUserId the recipient
     */
    void deleteByUserId(@NotNull String recipientUserId);

    /**
     * Count the number of notifications by user with the given status(es)
     */
    int countNotificationsByUserId(@NotNull String recipientUserId, List<NotificationStatus> notificationStatus);

    /**
     * Mark the notifications with the given IDs with the provided status.
     *
     * If a notification ID does not belong to recipientUserId, it will be silently ignored.
     */
    void markNotificationsWithStatusById(@NotNull String recipientUserId, @NotNull List<Long> idList, @NotNull NotificationStatus status) throws StorageException;

    /**
     * Mark all OPEN notifications as read for the given recipient.
     *
     * If a notification ID does not belong to recipientUserId, it will be silently ignored.
     */
    void markAllNotificationsReadByUserId(@NotNull String recipientUserId, @NotNull NotificationStatus status);

    /**
     * Get a user's notification preferences
     */
    Optional<NotificationPreferenceEntity> getNotificationPreferenceByUserIdAndType(@NotNull String userId, @NotNull String notificationType);
}
