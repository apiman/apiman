package io.apiman.manager.api.notifications.impl;

import io.apiman.manager.api.beans.events.AccountSignupEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.notifications.NotificationCategory;
import io.apiman.manager.api.notifications.INotificationProducer;
import io.apiman.manager.api.notifications.dto.CreateNotificationDto;
import io.apiman.manager.api.notifications.dto.RecipientType;
import io.apiman.manager.api.service.NotificationService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class NewAccountEventProcessor implements INotificationProducer {
    private final NotificationService notificationService;

    @Inject
    public NewAccountEventProcessor(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void processEvent(IVersionedApimanEvent event) {
        AccountSignupEvent signupEvent = (AccountSignupEvent) event;

        CreateNotificationDto newNotification = new CreateNotificationDto()
             .setRecipient("accountapprover") // TODO(msavy): take this from preferences
             .setRecipientType(RecipientType.ROLE)
             .setReason("apiman.account.created")
             .setReasonMessage()
             .setCategory(NotificationCategory.USER_ADMINISTRATION)
             .setPayload();

        notificationService.sendNotification(newNotification);
    }
}
