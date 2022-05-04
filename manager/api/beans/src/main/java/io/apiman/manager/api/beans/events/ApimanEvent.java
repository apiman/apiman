package io.apiman.manager.api.beans.events;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Decorate {@link IVersionedApimanEvent} with name and version metadata.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApimanEvent {

    /**
     * FQCN will be used if no value is set.
     * <p>
     * Setting custom names can let you emulate an older version of an event, for example.
     */
    String name() default "";

    /**
     * Version of the event
     */
    long version();
}
