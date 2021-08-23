package io.apiman.manager.api.jpa;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.ApimanEventHeaders;
import io.apiman.manager.api.beans.events.EventVersion;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.jpa.model.outbox.OutboxEventEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class TransactionalOutboxService extends AbstractJpaStorage {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(TransactionalOutboxService.class);
    private static final ObjectMapper OM = new ObjectMapper();


    @Inject
    public TransactionalOutboxService() {

    }

    public void insert(@Observes IVersionedApimanEvent apimanEvent) {
        ApimanEventHeaders headers = apimanEvent.getHeaders();
        long eventVersion = getEventVersion(apimanEvent, headers);
        String eventType = getType(apimanEvent);

        OutboxEventEntity outboxEvent = new OutboxEventEntity()
             .setEventVersion(eventVersion)
             .setType(eventType)
             .setSource(headers.getSource().toString())
             .setSubject(headers.getSubject())
             .setTime(headers.getTime())
             .setPayload(serializeWithoutHeaders(apimanEvent));

        EntityManager em = getActiveEntityManager();
        em.persist(outboxEvent);
        em.remove(outboxEvent);
        LOGGER.debug("Persisted event to transactional outbox & immediately deleted {0}", outboxEvent);
    }

    private JsonNode serializeWithoutHeaders(IVersionedApimanEvent apimanEvent) {
        OM.addMixIn(apimanEvent.getClass(), IgnoreHeadersMixin.class);
        return OM.valueToTree(apimanEvent);
    }

    private String getType(IVersionedApimanEvent apimanEvent) {
        String currentValue = apimanEvent.getHeaders().getType();
        if (StringUtils.isEmpty(currentValue)) {
            return apimanEvent.getClass().getCanonicalName();
        } else {
            return currentValue;
        }
    }

    private long getEventVersion(IVersionedApimanEvent event, ApimanEventHeaders headers) {
        // Version was not set, get it from the annotation if possible.
        if (headers.getEventVersion() <= 0) {
            EventVersion ev = headers.getClass().getAnnotation(EventVersion.class);
            if (ev == null) {
                LOGGER.warn("No event version set for {0}, defaulting to 1. "
                     + "This may cause unintended effects.", event.getClass().getCanonicalName());
                return 1;
            }
            return ev.value();
        } else {
            return headers.getEventVersion();
        }
    }

    private abstract static class IgnoreHeadersMixin {

        @JsonIgnore
        public abstract ApimanEventHeaders getHeaders();
    }
}
