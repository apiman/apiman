package io.apiman.manager.api.jpa;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;

/**
 * Produce the BlazePersistence criteria builder, a superior alternative to JPA CriteriaBuilder.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class CriteriaBuilderFactoryProducer {

    @Inject
    private EntityManagerFactoryAccessor emf;

    private volatile CriteriaBuilderFactory criteriaBuilderFactory;

    public CriteriaBuilderFactoryProducer() {}

    @PostConstruct
    public void construct() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        this.criteriaBuilderFactory = config.createCriteriaBuilderFactory(emf.getEntityManagerFactory());
    }

    @Produces
    @ApplicationScoped
    public CriteriaBuilderFactory createCriteriaBuilderFactory() {
        return criteriaBuilderFactory;
    }
}