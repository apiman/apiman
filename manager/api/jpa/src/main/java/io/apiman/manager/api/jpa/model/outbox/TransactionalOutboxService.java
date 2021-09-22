package io.apiman.manager.api.jpa.model.outbox;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.ApimanEvent;
import io.apiman.manager.api.beans.events.ApimanEventHeaders;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.jpa.AbstractJpaStorage;

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
 * Write event to the transactional outbox (to hook into Debezium or similar).
 *
 * <p>This will allow events to propagate outside the system boundaries.
 *
 * <p>See the following resources for more information:
 *
 * <ul>
 *     <li>
 *         <a href="https://microservices.io/patterns/data/transactional-outbox.html">
 *           Transactional Outbox pattern (general concepts)
 *         </a>
 *     </li>
 *     <li>
 *         <a href="https://debezium.io/blog/2019/02/19/reliable-microservices-data-exchange-with-the-outbox-pattern/">
 *             Transactional Outbox with Debezium
 *         </a>
 *     </li>
 * </ul>
 *
 * <p>In short, we write the event to the database and immediately delete it. At first glance this may seem strange.
 * However, this still ensures that the record is written to the transaction long and is recoverable by a CDC engine
 * such as Debezium. This allows this information to be reliably "pushed" into another platform such as Debezium.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class TransactionalOutboxService extends AbstractJpaStorage {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(TransactionalOutboxService.class);
    private static final ObjectMapper OM = new ObjectMapper();

    public TransactionalOutboxService() {}

    /**
     * Anything event fired that uses the {@link IVersionedApimanEvent} interface will be stored in the outbox.
     * Fire events using the CDI observable system; generally, they should be enrolled in a transaction to ensure
     * transactional properties.
     *
     * <p>This is then packed into an {@link OutboxEventEntity} and stored.
     *
     * <p>If the version number has not been set explicitly in the header segment by the implementor, it will be taken
     * from the {@link ApimanEvent} annotation, which can be placed on any {@link IVersionedApimanEvent} impl.
     * This allows multiple versions of an event to be detected/supported if that is necessary at some future point.
     *
     * <p>If the type of the event has not been set, the FQCN will be used (i.e. full class name).
     *
     * @param apimanEvent any {@link IVersionedApimanEvent} (usually fired by CDI observable subsystem).
     */
    public void onEvent(@Observes IVersionedApimanEvent apimanEvent) {
        ApimanEventHeaders headers = apimanEvent.getHeaders();
        String eventType = getType(apimanEvent);

        OutboxEventEntity outboxEvent = new OutboxEventEntity()
             .setEventVersion(headers.getEventVersion())
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

    // Serialize into Jackson JsonNode. Hibernate can serialize this into JSONB.
    private JsonNode serializeWithoutHeaders(IVersionedApimanEvent apimanEvent) {
        // Ignore #getHeaders as we're packing that in manually in #onEvent.
        OM.addMixIn(apimanEvent.getClass(), IgnoreHeadersMixin.class);
        return OM.valueToTree(apimanEvent);
    }

    /**
     * Use value set in headers or canonical class name
     */
    private String getType(IVersionedApimanEvent apimanEvent) {
        String currentValue = apimanEvent.getHeaders().getType();
        if (StringUtils.isEmpty(currentValue)) {
            return apimanEvent.getClass().getCanonicalName();
        } else {
            return currentValue;
        }
    }

    // Ignore #getHeaders as we're packing that in manually.
    private abstract static class IgnoreHeadersMixin {

        @JsonIgnore
        public abstract ApimanEventHeaders getHeaders();
    }
}
