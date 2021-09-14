package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.events.ContractApprovalRequestEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.notifications.impl.ContractApprovalRequestNotificationProducer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class ApiSignupApprovalRequest implements INotificationHandler  {

    @Inject
    public ApiSignupApprovalRequest() {}

    @Override
    public void handle(NotificationDto<? extends IVersionedApimanEvent> rawNotification) {
        @SuppressWarnings("unchecked")
        NotificationDto<ContractApprovalRequestEvent> signupNotification = (NotificationDto<ContractApprovalRequestEvent>) rawNotification;


    }

    @Override
    public boolean wants(NotificationDto<? extends IVersionedApimanEvent> notification) {
        if (notification.getReason().equals(ContractApprovalRequestNotificationProducer.APIMAN_API_APPROVAL_REQUEST)) {
            return true;
        }
        return false;
    }
}
