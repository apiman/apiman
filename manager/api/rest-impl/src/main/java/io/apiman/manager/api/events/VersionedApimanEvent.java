package io.apiman.manager.api.events;

/**
 * All Apiman events should extend this class.
 *
 * <p>Contains headers segment for various metadata that is useful to the event but does not pertain to the
 * semantics of the event itself (such as version, timestamps, etc.). This is mostly aligned with CloudEvents.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public abstract class VersionedApimanEvent {
    protected final ApimanEventHeaders headers;

    public VersionedApimanEvent(ApimanEventHeaders headers) {
        this.headers = headers;
    }

    public ApimanEventHeaders getHeaders() {
        return headers;
    }
}
