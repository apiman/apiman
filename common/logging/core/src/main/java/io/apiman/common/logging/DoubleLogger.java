package io.apiman.common.logging;

public class DoubleLogger implements IApimanLogger {

    private final IApimanLogger loggerA;
    private final IApimanLogger loggerB;

    public DoubleLogger(IApimanLogger loggerA, IApimanLogger loggerB) {
        this.loggerA = loggerA;
        this.loggerB = loggerB;
    }

    @Override
    public void info(String message) {
        loggerA.info(message);
        loggerB.info(message);
    }

    @Override
    public void info(String message, Object... args) {
        loggerA.info(message, args);
        loggerB.info(message, args);
    }

    @Override
    public void warn(String message) {
        loggerA.warn(message);
        loggerB.warn(message);
    }

    @Override
    public void warn(String message, Object... args) {
        loggerA.warn(message, args);
        loggerB.warn(message, args);
    }

    @Override
    public void debug(String message) {
        loggerA.debug(message);
        loggerB.debug(message);
    }

    @Override
    public void debug(String message, Object... args) {
        loggerA.debug(message, args);
        loggerB.debug(message, args);
    }

    @Override
    public void trace(String message) {
        loggerA.trace(message);
    }

    @Override
    public void trace(String message, Object... args) {
        loggerA.trace(message, args);
        loggerB.trace(message, args);
    }

    @Override
    public void error(Throwable error) {
        loggerA.error(error);
        loggerB.error(error);
    }

    @Override
    public void error(String message, Throwable error) {
        loggerA.error(message, error);
        loggerB.error(message, error);
    }

    @Override
    public void error(Throwable error, String message, Object... args) {
        loggerA.error(error, message, args);
        loggerB.error(error, message, args);
    }

}
