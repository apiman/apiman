package io.apiman.manager.api.rest.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.dto.NewAccountCreatedDto;
import io.apiman.manager.api.rest.IEventResource;
import io.apiman.manager.api.service.SsoEventService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * Simple HTTP event ingestion. For example, this provides a way for SSO to push "New Account Creation"
 * events to Apiman.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped // Could make this an @Alternative and not initialise it or noop for cases where we're using messaging, etc.
public class EventResourceImpl implements IEventResource {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(EventResourceImpl.class);

    private final SsoEventService ssoEventService;

    @Inject
    public EventResourceImpl(SsoEventService ssoEventService) {
        this.ssoEventService = ssoEventService;
    }

    // TODO: permissions check?
    @Override
    public void newAccountCreated(NewAccountCreatedDto newAccountCreatedDto) {
        LOGGER.debug("Received new account event via HTTP");
        ssoEventService.newAccountCreated(newAccountCreatedDto);
    }
}
