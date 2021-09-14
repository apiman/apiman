// package io.apiman.manager.api.notifications;
//
// import io.apiman.common.logging.ApimanLoggerFactory;
// import io.apiman.common.logging.IApimanLogger;
// import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
//
// import java.util.List;
// import java.util.stream.Collectors;
// import javax.enterprise.context.ApplicationScoped;
// import javax.enterprise.event.Observes;
// import javax.enterprise.inject.Any;
// import javax.enterprise.inject.Instance;
// import javax.inject.Inject;
//
// /**
//  * Observe {@link IVersionedApimanEvent} and route to Notification Producers that may be interested in the event.
//  *
//  * At the moment this does nothing sophisticated. We may be able to just eliminate this class?
//  *
//  * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
//  */
// @ApplicationScoped
// public class EventToNotificationDispatcher {
//     private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(EventToNotificationDispatcher.class);
//     // TODO: we could consider being smarter about this and using topic or prefix-based filtering.
//     private final List<INotificationProducer> notificationProducers;
//
//     @Inject
//     public EventToNotificationDispatcher(@Any Instance<INotificationProducer> notificationProducers) {
//         this.notificationProducers = notificationProducers.stream().collect(Collectors.toList());
//     }
//
//     public void on(@Observes IVersionedApimanEvent event) {
//         LOGGER.debug("Notification handler listened to an event: {0}", event);
//         for (INotificationProducer processor : notificationProducers) {
//                 processor.processEvent(event);
//         }
//     }
// }
