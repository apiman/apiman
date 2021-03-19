package io.apiman.common.logging;

import io.apiman.common.logging.impl.SystemOutLogger;

import java.util.Map;

/**
 * Please use {@link ApimanLoggerFactory} instead.
 *
 * @see ApimanLoggerFactory#getLogger(String)
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@Deprecated
public class DefaultDelegateFactory implements IDelegateFactory {

    static {
        ApimanLoggerFactoryRegistry.register("default", new DefaultDelegateFactory());
    }

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
