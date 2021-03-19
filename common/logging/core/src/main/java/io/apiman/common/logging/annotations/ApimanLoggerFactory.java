package io.apiman.common.logging.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Label logging delegate factories.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ApimanLoggerFactory {
    String name();
}
