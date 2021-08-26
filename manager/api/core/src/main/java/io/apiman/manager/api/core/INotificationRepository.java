package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.notifications.NotificationEntity;
import io.apiman.manager.api.core.exceptions.StorageException;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface INotificationRepository {

    void create(NotificationEntity bean) throws StorageException;

    void update(NotificationEntity bean) throws StorageException;

    void delete(NotificationEntity bean) throws StorageException;

    void deleteById(Long id) throws StorageException;
}
