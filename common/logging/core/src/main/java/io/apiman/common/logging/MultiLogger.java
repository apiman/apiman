package io.apiman.common.logging;

import java.util.List;
import java.util.function.Consumer;

public class MultiLogger implements IApimanLogger {

    private final List<IApimanLogger> delegates;

    public MultiLogger(List<IApimanLogger> delegates) {
        this.delegates = delegates;
    }

    @Override
    public void info(String message) {
        doLogging(delegateLogger -> delegateLogger.info(message));
    }

    @Override
    public void info(String message, Object... args) {
        doLogging(delegateLogger -> delegateLogger.info(message, args));
    }

    @Override
    public void warn(String message) {
        doLogging(delegateLogger -> delegateLogger.warn(message));
    }

    @Override
    public void warn(String message, Object... args) {
        doLogging(delegateLogger -> delegateLogger.warn(message, args));
    }

    @Override
    public void debug(String message) {
        doLogging(delegateLogger -> delegateLogger.debug(message));
    }

    @Override
    public void debug(String message, Object... args) {
        doLogging(delegateLogger -> delegateLogger.debug(message, args));
    }

    @Override
    public void trace(String message) {
        doLogging(delegateLogger -> delegateLogger.trace(message));
    }

    @Override
    public void trace(String message, Object... args) {
        doLogging(delegateLogger -> delegateLogger.trace(message, args));
    }

    @Override
    public void error(Throwable error) {
        doLogging(delegateLogger -> delegateLogger.error(error));
    }

    @Override
    public void error(String message, Throwable error) {
        doLogging(delegateLogger -> delegateLogger.error(message, error));
    }

    @Override
    public void error(Throwable error, String message, Object... args) {
        doLogging(delegateLogger -> delegateLogger.error(error, message, args));
    }

    private void doLogging(Consumer<IApimanLogger> consumerFunction) {
        for (IApimanLogger delegate : delegates) {
            consumerFunction.accept(delegate);
        }
    }
}
