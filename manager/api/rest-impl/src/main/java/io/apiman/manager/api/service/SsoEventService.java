package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.AccountSignupEvent;
import io.apiman.manager.api.beans.events.ApimanEventHeaders;
import io.apiman.manager.api.beans.events.dto.NewAccountCreatedDto;
import io.apiman.manager.api.events.EventService;

import java.net.URI;
import java.time.OffsetDateTime;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class SsoEventService {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(SsoEventService.class);
    private EventService eventService;

    @Inject
    public SsoEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public SsoEventService() {
    }

    public void newAccountCreated(NewAccountCreatedDto newAccountCreatedDto) {
        ApimanEventHeaders headers = ApimanEventHeaders
             .builder()
             .setId(key(newAccountCreatedDto.getUserId(), newAccountCreatedDto.getTime()))
             .setSource(URI.create("http://replaceme.local/foo"))
             .setSubject("new.account")
             .build();

        AccountSignupEvent accountSignup = AccountSignupEvent
             .builder()
             .setHeaders(headers)
             .setUserId(newAccountCreatedDto.getUserId())
             .setUsername(newAccountCreatedDto.getUsername())
             .setEmailAddress(newAccountCreatedDto.getEmailAddress())
             .setFirstName(newAccountCreatedDto.getFirstName())
             .setSurname(newAccountCreatedDto.getSurname())
             .build();

        LOGGER.debug("Received an account creation event (externally): {0} => translated into: {1}",
             newAccountCreatedDto, accountSignup);

        eventService.fireEvent(accountSignup);
    }

    private static String key(String userId, OffsetDateTime createdOn) {
        return String.join("-", userId, createdOn.toString()); // TODO(msavy): userId alone might be good enough?
    }
}
