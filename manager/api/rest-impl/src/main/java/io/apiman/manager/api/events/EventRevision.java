package io.apiman.manager.api.events;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Revision of event
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventRevision {
    int value();
}
