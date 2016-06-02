package io.apiman.common.logging;

import io.apiman.common.logging.impl.SystemOutLogger;

import java.util.Map;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
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
