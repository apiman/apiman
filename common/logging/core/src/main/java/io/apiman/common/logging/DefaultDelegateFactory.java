package io.apiman.common.logging;

import io.apiman.common.logging.impl.SystemOutLogger;
import io.apiman.common.logging.annotations.ApimanLoggerFactory;

import java.util.Map;

/**
 * Please use {@link io.apiman.common.logging.ApimanLoggerFactory} instead.
 *
 * @see io.apiman.common.logging.ApimanLoggerFactory#getLogger(Class)
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@Deprecated
@ApimanLoggerFactory(name = "default")
public class DefaultDelegateFactory implements IDelegateFactory {

    public DefaultDelegateFactory() {
    }

    public DefaultDelegateFactory(Map<String, String> opts) {
    }

    @Override
    public IApimanLogger createLogger(String name) {
        return new SystemOutLogger();
    }

    @Override
    public IApimanLogger createLogger(Class<?> klazz) {
        return new SystemOutLogger();
    }
}
