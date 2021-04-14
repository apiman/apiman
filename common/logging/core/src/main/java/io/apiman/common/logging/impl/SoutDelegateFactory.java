package io.apiman.common.logging.impl;

import io.apiman.common.logging.ApimanLoggerFactoryRegistry;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.logging.IDelegateFactory;
import io.apiman.common.logging.annotations.ApimanLoggerFactory;

import java.util.Map;

/**
 * If you are using this, you probably forgot to select a good logger.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApimanLoggerFactory("sout")
public class SoutDelegateFactory implements IDelegateFactory {

    public SoutDelegateFactory() {
    }

    public SoutDelegateFactory(Map<String, String> opts) {
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
