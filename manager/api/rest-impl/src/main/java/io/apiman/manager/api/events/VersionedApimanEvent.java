package io.apiman.manager.api.events;

import java.net.URI;
import java.time.OffsetDateTime;

/**
 * Uses cloud events-style fields
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public abstract class VersionedApimanEvent {
    String id;
    String eventVersion;
    URI source;
    String type;
    String subject;
    OffsetDateTime time;
    ApimanEventData data; // Actual event

    public VersionedApimanEvent() {

    }
}
