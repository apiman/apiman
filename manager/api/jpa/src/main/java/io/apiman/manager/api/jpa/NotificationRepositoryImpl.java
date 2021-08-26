package io.apiman.manager.api.jpa;

import io.apiman.manager.api.beans.notifications.NotificationEntity;
import io.apiman.manager.api.core.INotificationRepository;
import io.apiman.manager.api.core.exceptions.StorageException;

import javax.enterprise.context.ApplicationScoped;

/**
 * Storage for simple notifications system
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class NotificationRepositoryImpl extends AbstractJpaStorage implements INotificationRepository {

    public NotificationRepositoryImpl() {
        super();
    }

    @Override
    public void create(NotificationEntity bean) throws StorageException {
        super.create(bean);
    }

    @Override
    public void update(NotificationEntity bean) throws StorageException {
        super.update(bean);
    }

    @Override
    public void delete(NotificationEntity bean) throws StorageException {
        super.delete(bean);
    }

    @Override
    public void deleteById(Long id) throws StorageException {
        delete(super.get(id, NotificationEntity.class));
    }

    // public <T> T get(Long id, Class<T> type) throws StorageException {
    //     return super.get(id, type);
    // }
    //
    // public <T> T get(String id, Class<T> type) throws StorageException {
    //     return super.get(id, type);
    // }
}
