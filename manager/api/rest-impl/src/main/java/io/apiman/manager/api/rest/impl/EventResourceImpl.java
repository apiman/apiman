package io.apiman.manager.api.rest.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.dto.NewAccountCreatedDto;
import io.apiman.manager.api.rest.IEventResource;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.service.SsoEventService;

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
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(EventResourceImpl.class);

    private ISecurityContext securityContext;
    private SsoEventService ssoEventService;

    @Inject
    public EventResourceImpl(ISecurityContext securityContext, SsoEventService ssoEventService) {
        this.securityContext = securityContext;
        this.ssoEventService = ssoEventService;
    }

    public EventResourceImpl() {
    }

    // TODO: permissions check?
    @Override
    public void newAccountCreated(NewAccountCreatedDto newAccountCreatedDto) {
        securityContext.checkAdminPermissions();
        LOGGER.debug("Received new account event via HTTP");
        ssoEventService.newAccountCreated(newAccountCreatedDto);
    }
}
