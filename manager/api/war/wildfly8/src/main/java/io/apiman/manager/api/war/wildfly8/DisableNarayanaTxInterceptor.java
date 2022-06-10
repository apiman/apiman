package io.apiman.manager.api.war.wildfly8;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
//META-INF/services/jakarta.enterprise.inject.spi.Extension
public class DisableNarayanaTxInterceptor implements javax.enterprise.inject.spi.Extension {

    <T> void processAnnotatedType(@Observes @WithAnnotations({ javax.interceptor.Interceptor.class}) ProcessAnnotatedType<T> pat) {
        /* tell the container to ignore the type if it is annotated @Ignore */
        if (pat.getAnnotatedType().isAnnotationPresent(javax.interceptor.Interceptor.class)
        && pat.getAnnotatedType().getJavaClass().getName().startsWith("com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptor")) {
            pat.veto();
            System.out.println("Vetoed Narayana interceptor " + pat.getAnnotatedType().getJavaClass().getName());
        }
    }
}
