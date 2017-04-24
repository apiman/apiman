package io.apiman.gateway.platforms.vertx3.logging;

import io.vertx.core.spi.logging.LogDelegate;
import io.vertx.core.spi.logging.LogDelegateFactory;

public class ApimanLog4j2LogDelegateFactory implements LogDelegateFactory {
    @Override
    public LogDelegate createDelegate(final String name) {
       return new ApimanLog4j2LogDelegate(name);
    }
}