package io.apiman.manager.api.events;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class AccountSignupEvent extends VersionedApimanEvent {
    private String userId;
    // What details in here, hmm!

    public AccountSignupEvent(ApimanEventHeaders headers) {
        super(headers);
    }
}
