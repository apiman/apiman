package io.apiman.manager.api.jpa.model.outbox;

import io.apiman.manager.api.beans.events.AccountSignupEvent;
import io.apiman.manager.api.beans.events.ApimanEventHeaders;
import io.apiman.manager.api.beans.events.EventVersion;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class OutboxEventEntityTest {
    private static final ObjectMapper OM = new ObjectMapper();

    @Test
    public void test() {
        ApimanEventHeaders headers = ApimanEventHeaders
             .builder()
             .setId("test")
             .setSource(URI.create("http://replaceme.local/foo"))
             .setSubject("SsoNewAccount")
             .build();

        AccountSignupEvent apimanEvent = AccountSignupEvent
             .builder()
             .setHeaders(headers)
             .setUserId("fb0123")
             .setUsername("msavy")
             .setEmailAddress("email@address.local")
             .setFirstName("John")
             .setSurname("Smith")
             .build();

        long eventVersion = getEventVersion(apimanEvent, headers);
        String eventType = getType(apimanEvent);

        OutboxEventEntity outboxEvent = new OutboxEventEntity()
             .setEventVersion(eventVersion)
             .setType(eventType)
             .setSource(headers.getSource().toString())
             .setSubject(headers.getSubject())
             .setTime(headers.getTime())
             .setPayload(serializeWithoutHeaders(apimanEvent));

        System.out.println("OK worked " + outboxEvent);
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
            EventVersion ev = event.getClass().getAnnotation(EventVersion.class);
            if (ev == null) {
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
