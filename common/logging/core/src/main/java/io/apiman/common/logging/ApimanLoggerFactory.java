package io.apiman.common.logging;

public class ApimanLoggerFactory {

    private static volatile boolean LOGGER_RESOLVED = false;
    private static IDelegateFactory LOGGER_FACTORY;

    public static IApimanLogger getLogger(String name) {
        return getLoggerFactory().createLogger(name);
    }

    public static IApimanLogger getLogger(Class<?> klazz) {
        return getLoggerFactory().createLogger(klazz);
    }

    private static IDelegateFactory getLoggerFactory() {
        if (!LOGGER_RESOLVED) {
            synchronized (ApimanLoggerFactory.class) {
                if (!LOGGER_RESOLVED) {
                    LOGGER_FACTORY = resolveLoggerFactory();
                    LOGGER_RESOLVED = true;
                }
            }
        }
        return LOGGER_FACTORY;
    }

    private static IDelegateFactory resolveLoggerFactory() {
        String sysProp = System.getProperty("io.apiman.logger");
        if (sysProp == null) {
            return ApimanLoggerFactoryRegistry.getDefaultLoggerFactory();
        }
        return ApimanLoggerFactoryRegistry.getLoggerFactory(sysProp);
    }

    public synchronized static void setDelegate(IDelegateFactory delegate) {
        LOGGER_FACTORY = delegate;
        LOGGER_RESOLVED = true;
    }
}
