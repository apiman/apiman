package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface INotificationHandler<T extends IVersionedApimanEvent> {
    Set<String> WARN_ONCE_SET = new HashSet<>();
    IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(INotificationHandler.class);

    void handle(NotificationDto<T> notification, Map<String, Object> defaultTemplateMap);

    boolean wants(NotificationDto<? extends IVersionedApimanEvent> notification);

    default void warnOnce(UserDto user, NotificationDto<? extends IVersionedApimanEvent> notification) {
        String langTag = user.getLocale().toLanguageTag();
        String reason = notification.getReason();
        String key = reason + langTag;
        if (!WARN_ONCE_SET.contains(key)) {
            LOGGER.warn("No email notification template could be resolved for {0} with locale {1}. "
                                + "No email notification will be sent, and all future notifications of this same type will be dropped.", reason, langTag);
            WARN_ONCE_SET.add(key);
        }
    }
}
