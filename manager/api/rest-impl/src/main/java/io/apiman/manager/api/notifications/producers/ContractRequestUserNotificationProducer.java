/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

package io.apiman.manager.api.notifications.producers;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.events.ContractCreatedEvent;
import io.apiman.manager.api.beans.notifications.NotificationCategory;
import io.apiman.manager.api.beans.notifications.dto.CreateNotificationDto;
import io.apiman.manager.api.beans.notifications.dto.RecipientDto;
import io.apiman.manager.api.beans.notifications.dto.RecipientType;
import io.apiman.manager.api.notifications.INotificationProducer;
import io.apiman.manager.api.service.NotificationService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

/**
 * Accept a {@link ContractCreatedEvent} and produce a {@link #APIMAN_CLIENT_CONTRACT_REQUEST_REASON} notification.
 * This is a summary for the user who requested an API to inform him, that is request is pending approval.
 *
 * @author Florian Volk {@literal <florian.volk@scheer-group.com>}
 */
@ApplicationScoped
public class ContractRequestUserNotificationProducer implements INotificationProducer {

    public static final String APIMAN_CLIENT_CONTRACT_REQUEST_REASON = "apiman.client.contract.request.user";

    private final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ContractRequestUserNotificationProducer.class);
    private final NotificationService notificationService;

    @Inject
    public ContractRequestUserNotificationProducer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void processEvent(@Observes ContractCreatedEvent signupEvent) {
        LOGGER.debug("Processing signup event {0}", signupEvent);
        if (signupEvent.isApprovalRequired()) {
            CreateNotificationDto newNotification = new CreateNotificationDto();
            String orgId = signupEvent.getApiOrgId();

            RecipientDto requester = new RecipientDto()
                 .setRecipient(signupEvent.getUser().getUsername())
                 .setOrgId(orgId)
                 .setRecipientType(RecipientType.INDIVIDUAL);

            newNotification.setRecipient(List.of(requester))
                           .setReason(APIMAN_CLIENT_CONTRACT_REQUEST_REASON)
                           .setReasonMessage("Signup for API")
                           .setCategory(NotificationCategory.API_LIFECYCLE)
                           .setSource("/apiman/notifications/contracts/approvals")
                           .setPayload(signupEvent);

            LOGGER.debug("Sending notification for user requested contract {0} from client {1} version {2}",
                 signupEvent.getContractId(), signupEvent.getClientId(), signupEvent.getClientVersion());

            notificationService.sendNotification(newNotification);
        }
    }
}
