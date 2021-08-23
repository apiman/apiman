package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.AccountSignupEvent;
import io.apiman.manager.api.beans.events.ApimanEventHeaders;
import io.apiman.manager.api.beans.events.dto.NewAccountCreatedDto;

import java.net.URI;
import java.time.OffsetDateTime;
import javax.enterprise.context.ApplicationScoped;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class SsoEventService {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(SsoEventService.class);

    public SsoEventService() {
    }

    public void newAccountCreated(NewAccountCreatedDto newAccountCreatedDto) {

        ApimanEventHeaders headers = ApimanEventHeaders
             .builder()
             .setId(key(newAccountCreatedDto.getUserId(), newAccountCreatedDto.getTime()))
             .setSource(URI.create("http://replaceme.local/foo"))
             .setSubject("SsoNewAccount")
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

        LOGGER.debug("Received an account creation event (externally): {0}. \n "
             + "Translated into: {1}", newAccountCreatedDto, accountSignup);
    }

    private static String key(String userId, OffsetDateTime createdOn) {
        return String.join("-", userId, createdOn.toString()); // TODO(msavy): userId alone might be good enough?
    }
}
