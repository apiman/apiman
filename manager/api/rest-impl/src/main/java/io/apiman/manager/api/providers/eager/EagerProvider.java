package io.apiman.manager.api.providers.eager;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.util.AnnotationLiteral;
import javax.ws.rs.ext.Provider;

/**
 * Thanks to Dan Allen <a href="https://gist.github.com/mojavelinux/635719#file-startupbeanextension-java-L19">
 * for this trick to eager initialise</a> managed beans in CDI.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Provider
public class EagerProvider implements Extension {

    public void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
        beanManager
             .getBeans(Object.class, new AnnotationLiteral<EagerLoaded>() {})
             .parallelStream()
             .forEach(bean -> prod(bean, beanManager));
    }

    /**
     * Call #toString to trigger the managed bean to be initialised.
     */
    private void prod(Bean<?> bean, BeanManager beanManager) {
        beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean)).toString();
    }
}
