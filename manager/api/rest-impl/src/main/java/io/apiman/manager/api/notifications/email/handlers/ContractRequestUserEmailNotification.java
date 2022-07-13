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

package io.apiman.manager.api.notifications.email.handlers;

import io.apiman.manager.api.beans.events.ContractCreatedEvent;
import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.notifications.EmailNotificationTemplate;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.notifications.email.SimpleEmail;
import io.apiman.manager.api.notifications.email.SimpleMailNotificationService;
import io.apiman.manager.api.notifications.producers.ContractRequestUserNotificationProducer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Map;

/**
 * @author Florian Volk {@literal <florian.volk@scheer-group.com>}
 */
@ApplicationScoped
public class ContractRequestUserEmailNotification implements INotificationHandler<ContractCreatedEvent> {

    private final SimpleMailNotificationService mailNotificationService;

    @Inject
    public ContractRequestUserEmailNotification(SimpleMailNotificationService mailNotificationService) {
        this.mailNotificationService = mailNotificationService;
    }

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        // no-op to force eager initialization
    }

    @Override
    public void handle(NotificationDto<ContractCreatedEvent> notification, Map<String, Object> defaultTemplateMap) {
        ContractCreatedEvent event = notification.getPayload();

        if (event.isApprovalRequired()) {
            UserDto recipient = notification.getRecipient();

            mailNotificationService
                    .findTemplateFor(notification.getReason(), recipient.getLocale())
                    .ifPresentOrElse(
                            template -> send(recipient, template, defaultTemplateMap),
                            () -> warnOnce(recipient, notification)
                    );
        }
    }

    private void send(UserDto recipient, EmailNotificationTemplate template, Map<String, Object> defaultTemplateMap) {
        var mail = SimpleEmail
             .builder()
             .setRecipient(recipient)
             .setTemplate(template)
             .setTemplateVariables(defaultTemplateMap)
             .build();

        mailNotificationService.send(mail);
    }

    @Override
    public boolean wants(NotificationDto<? extends IVersionedApimanEvent> notification) {
        return notification.getReason().equals(ContractRequestUserNotificationProducer.APIMAN_CLIENT_CONTRACT_REQUEST_REASON);
    }
}
