package io.apiman.manager.api.notifications.email;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.notifications.NotificationType;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.core.config.ApiManagerConfig;
import io.apiman.manager.api.notifications.email.handlers.INotificationHandler;
import io.apiman.manager.api.service.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class EmailNotificationDispatcher {
    private final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(EmailNotificationDispatcher.class);
    private final List<INotificationHandler<?>> handlers;
    private final NotificationService notificationService;
    private final ApiManagerConfig config;

    @Inject
    public EmailNotificationDispatcher(@Any Instance<INotificationHandler<?>> handlers,
                                       NotificationService notificationService,
                                       ApiManagerConfig config) {
        this.handlers = handlers.stream().collect(Collectors.toList());
        this.notificationService = notificationService;
        this.config = config;
    }

    /**
     * Observe, filter, and process notifications.
     *
     * <ul>
     *     <li>Get email preferences for intended recipient</li>
     *     <li>Determine whether user wants emails for category notification belongs to</li>
     *     <li>If so, process notification, else drop through silently</li>
     * </ul>
     */
    public void processNotification(@ObservesAsync NotificationDto<?> notification) {
        boolean wantsNotification = notificationService.userWantsNotification(
                notification.getRecipient().getUsername(),
                NotificationType.EMAIL,
                notification
        );
        if (wantsNotification) {
            dispatch(notification);
        } else {
            LOGGER.trace("Notification recipient did not want an email for {0}", notification);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void dispatch(NotificationDto<?> notification) {
        // LOGGER.trace("User has an email notification preference: {0}", emailPrefs);
        LOGGER.trace("Notification is a candidate for processing: {0}", notification);
        // TODO can easily optimise this for prefix matching (regex or whatever).
        Map<String, Object> defaultTemplateMap = createDefaultTemplateMap(notification, config);
        handlers.stream()
             .filter(handler -> handler.wants(notification))
             .forEach(handler -> handler.handle((NotificationDto) notification, defaultTemplateMap));
    }

    public static Map<String, Object> createDefaultTemplateMap(NotificationDto<?> notification, ApiManagerConfig config) {
        return Map.of(
                "notification", notification,
                "event", notification.getPayload(),
                "apiman-manager-ui-endpoint", StringUtils.removeEnd(config.getApimanManagerUiEndpoint(), "/")
        );
    }
}
