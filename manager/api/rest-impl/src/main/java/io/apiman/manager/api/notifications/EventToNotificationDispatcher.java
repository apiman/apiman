package io.apiman.manager.api.notifications;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class EventToNotificationDispatcher {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(EventToNotificationDispatcher.class);
    // TODO: we could consider being smarter about this and using topic or prefix-based filtering.
    private final List<INotificationProducer> notificationProducers;

    @Inject
    public EventToNotificationDispatcher(List<INotificationProducer> notificationProducers) {
        this.notificationProducers = notificationProducers;
    }

    public void on(@Observes IVersionedApimanEvent event) {
        LOGGER.debug("Notification handler listened to an event: {0}", event);
        for (INotificationProducer processor : notificationProducers) {
            // if (processor.isInterestedIn(event.getHeaders().getType())) {
                processor.processEvent(event);
            // }
        }
    }

    // private void routeEvent(IVersionedApimanEvent event) {
    //
    // }
}
