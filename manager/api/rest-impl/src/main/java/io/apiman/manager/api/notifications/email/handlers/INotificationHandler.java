package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface INotificationHandler {

    void handle(NotificationDto<? extends IVersionedApimanEvent> notification);

    boolean wants(NotificationDto<? extends IVersionedApimanEvent> notification);
}
