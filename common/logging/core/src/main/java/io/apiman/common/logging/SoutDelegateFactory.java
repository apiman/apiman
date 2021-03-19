package io.apiman.common.logging;

import io.apiman.common.logging.impl.SystemOutLogger;
import java.util.Map;

/**
 * If you are using this, you probably forgot to select a good logger.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class SoutDelegateFactory implements IDelegateFactory {

    static {
        ApimanLoggerFactoryRegistry.register("default", new SoutDelegateFactory());
    }

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
