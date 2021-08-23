package io.apiman.manager.api.jpa;

import io.apiman.manager.api.core.ITransactionalOutbox;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class TransactionalOutbox implements ITransactionalOutbox {

    @Inject
    public TransactionalOutbox() {

    }


}
