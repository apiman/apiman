package io.apiman.common.logging;

import io.apiman.common.logging.annotations.ApimanLoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

/**
 * Registry of Apiman Logger Factories
 */
public class ApimanLoggerFactoryRegistry {
    private static final Map<String, IDelegateFactory> DELEGATE_FACTORY_MAP = new HashMap<>();
    private static IDelegateFactory  DEFAULT_FACTORY = new SoutDelegateFactory();

    static {
        resolveAllImplementations();
    }

    private static void resolveAllImplementations() {
        Reflections reflection = new Reflections("io.apiman.common.logging");
        Set<Class<?>> loggerFactories = reflection.getTypesAnnotatedWith(ApimanLoggerFactory.class);

        for (Class<?> loggerFactory : loggerFactories) {
            try {
                IDelegateFactory instance = (IDelegateFactory) loggerFactory.newInstance();
                String name = loggerFactory.getAnnotation(ApimanLoggerFactory.class).name();
                DELEGATE_FACTORY_MAP.put(name, instance);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void register(String name, IDelegateFactory loggerFactory) {
        DELEGATE_FACTORY_MAP.put(name, loggerFactory);
    }

    public static void registerAsDefault(String name, IDelegateFactory loggerFactory) {
        DELEGATE_FACTORY_MAP.put(name, loggerFactory);
        DEFAULT_FACTORY = loggerFactory;
    }

    static IDelegateFactory getLoggerFactory(String name) {
        return DELEGATE_FACTORY_MAP.get(name);
    }

    static IDelegateFactory getDefaultLoggerFactory() {
        return DEFAULT_FACTORY;
    }
}
