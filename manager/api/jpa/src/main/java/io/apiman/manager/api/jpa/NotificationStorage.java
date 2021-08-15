package io.apiman.manager.api.jpa;

import io.apiman.manager.api.core.INotificationRepository;

/**
 * Storage for simple notifications system
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class NotificationStorage extends AbstractJpaStorage implements INotificationRepository {

    public NotificationStorage() {
        super();
    }
}
