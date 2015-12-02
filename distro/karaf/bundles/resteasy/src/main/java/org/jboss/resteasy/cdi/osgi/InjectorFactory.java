package org.jboss.resteasy.cdi.osgi;

import org.jboss.resteasy.cdi.CdiConstructorInjector;
import org.jboss.resteasy.cdi.CdiPropertyInjector;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.cdi.i18n.LogMessages;
import org.jboss.resteasy.cdi.i18n.Messages;
import org.jboss.resteasy.core.ValueInjector;
import org.jboss.resteasy.spi.ConstructorInjector;
import org.jboss.resteasy.spi.MethodInjector;
import org.jboss.resteasy.spi.PropertyInjector;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.ServiceReference;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

public class InjectorFactory implements org.jboss.resteasy.spi.InjectorFactory {
    private ResteasyProviderFactory providerFactory;
    private org.jboss.resteasy.spi.InjectorFactory delegate;
    private BeanManager manager;
    private ResteasyCdiExtension extension;
    private Map<Class<?>, Type> sessionBeanInterface;

    public InjectorFactory(ResteasyProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
        this.delegate = providerFactory.getInjectorFactory();
        this.manager = lookupBeanManager();
        this.extension = lookupResteasyCdiExtension();
        sessionBeanInterface = extension.getSessionBeanInterface();
    }

    public ConstructorInjector createConstructor(Constructor constructor) {
        Class<?> clazz = constructor.getDeclaringClass();

        if (!manager.getBeans(clazz).isEmpty()) {
            LogMessages.LOGGER.debug(Messages.MESSAGES.usingCdiConstructorInjector(clazz));
            return new CdiConstructorInjector(clazz, manager);
        }

        if (sessionBeanInterface.containsKey(clazz)) {
            Type intfc = sessionBeanInterface.get(clazz);
            LogMessages.LOGGER.debug(Messages.MESSAGES.usingInterfaceForLookup(intfc, clazz));
            return new CdiConstructorInjector(intfc, manager);
        }

        LogMessages.LOGGER.debug(Messages.MESSAGES.noCDIBeansFound(clazz));
        return delegate.createConstructor(constructor);
    }

    public MethodInjector createMethodInjector(Class root, Method method) {
        return delegate.createMethodInjector(root, method);
    }

    public PropertyInjector createPropertyInjector(Class resourceClass) {
        return new CdiPropertyInjector(delegate.createPropertyInjector(resourceClass), resourceClass,
                sessionBeanInterface, manager);
    }

    public ValueInjector createParameterExtractor(Class injectTargetClass, AccessibleObject injectTarget,
            Class type, Type genericType, Annotation[] annotations) {
        return delegate
                .createParameterExtractor(injectTargetClass, injectTarget, type, genericType, annotations);
    }

    public ValueInjector createParameterExtractor(Class injectTargetClass, AccessibleObject injectTarget,
            Class type, Type genericType, Annotation[] annotations, boolean useDefault) {
        return delegate
                .createParameterExtractor(injectTargetClass, injectTarget, type, genericType, annotations,
                        useDefault);
    }

    /**
     * Do a lookup for BeanManager instance. OSGI BeanManager Service is searched.
     *
     * @return BeanManager instance
     */
    protected BeanManager lookupBeanManager() {

        BeanManager beanManager = null;

        BundleContext ctx =
                BundleReference.class.cast(InjectorFactory.class.getClassLoader())
                        .getBundle()
                        .getBundleContext();
        ServiceReference reference = ctx.getServiceReference(BeanManager.class.getName());
        beanManager = (BeanManager) ctx.getService(reference);
        if (beanManager != null) {
            LogMessages.LOGGER.debug("BeanManager retrieved as OSGI Service");
            return beanManager;
        }

        throw new RuntimeException(Messages.MESSAGES.unableToLookupBeanManager());
    }

    /**
     * Lookup ResteasyCdiExtension instance that was instantiated during CDI bootstrap
     *
     * @return ResteasyCdiExtension instance
     */
    private ResteasyCdiExtension lookupResteasyCdiExtension() {
        Set<Bean<?>> beans = manager.getBeans(ResteasyCdiExtension.class);
        Bean<?> bean = manager.resolve(beans);
        if (bean == null) {
            throw new IllegalStateException(Messages.MESSAGES.unableToObtainResteasyCdiExtension());
        }
        CreationalContext<?> context = manager.createCreationalContext(bean);
        return (ResteasyCdiExtension) manager.getReference(bean, ResteasyCdiExtension.class, context);
    }
}
