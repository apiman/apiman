package io.apiman.manager.api.notifications.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.notifications.INotificationProducer;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class ApiSignupNotificationProducer implements INotificationProducer  {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ApiSignupNotificationProducer.class);
    public static final String APIMAN_API_APPROVAL_REQUEST = "apiman.api.approval.request";

    @Override
    public void processEvent(IVersionedApimanEvent notification) {

    }
}
