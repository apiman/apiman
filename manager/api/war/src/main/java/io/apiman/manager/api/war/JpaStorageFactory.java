package io.apiman.manager.api.war;

import io.apiman.manager.api.jpa.JpaStorage;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.elasticsearch.common.inject.ProvidedBy;
import org.elasticsearch.common.inject.Provides;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class JpaStorageFactory {

    @Inject
    public JpaStorageFactory() {
    }

    public JpaStorage produceJpaStorage() {
        return new JpaStorage();
    }
}
