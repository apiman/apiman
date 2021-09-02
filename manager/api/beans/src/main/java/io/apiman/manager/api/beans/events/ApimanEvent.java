package io.apiman.manager.api.beans.events;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApimanEvent {
    String name() default "";
    int version() default 1;
}
