package io.apiman.manager.api.rest.impl;

import io.apiman.manager.api.beans.events.dto.NewAccountCreatedDto;
import io.apiman.manager.api.rest.IEventResource;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Simple HTTP event ingestion. For example, this provides a way for SSO to push "New Account Creation"
 * events to Apiman.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped // Could make this an @Alternative and not initialise it or noop for cases where we're using messaging, etc.
public class EventResourceImpl implements IEventResource {


    @Inject
    public EventResourceImpl() {
    }


    @Override
    public void notifyNewAccount(NewAccountCreatedDto newAccountCreatedDto) {

    }
}
