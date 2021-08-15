package io.apiman.manager.api.events;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApiSignupEvent {
    String userId;
    String clientId;
    String apiId;
    String contractId;
    boolean approvalRequired;
}
