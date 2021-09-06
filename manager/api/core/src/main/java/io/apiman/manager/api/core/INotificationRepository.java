package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.notifications.NotificationEntity;
import io.apiman.manager.api.beans.notifications.NotificationPreferenceEntity;
import io.apiman.manager.api.beans.notifications.NotificationStatus;
import io.apiman.manager.api.beans.search.PagingBean;
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

    NotificationEntity getNotificationById(@NotNull Long notificationId) throws StorageException;

    SearchResultsBean<NotificationEntity> getUnreadNotificationsByRecipientId(@NotNull String recipientUserId, @Nullable PagingBean pagingBean)
         throws StorageException;

    void create(@NotNull NotificationEntity bean) throws StorageException;

    void update(@NotNull NotificationEntity bean) throws StorageException;

    void delete(@NotNull NotificationEntity bean) throws StorageException;

    void deleteById(@NotNull Long id) throws StorageException;

    void deleteAll();

    void deleteByUserId(@NotNull String recipientUserId);

    int countUnreadNotificationsByUserId(@NotNull String recipientUserId);

    void markNotificationsReadById(@NotNull String recipientUserId, @NotNull List<Long> idList, @NotNull NotificationStatus status) throws StorageException;

    void markAllNotificationsReadByUserId(@NotNull String recipientUserId, @NotNull NotificationStatus status);

    Optional<NotificationPreferenceEntity> getNotificationPreferenceByUserIdAndType(String userId, String notificationType);
}
