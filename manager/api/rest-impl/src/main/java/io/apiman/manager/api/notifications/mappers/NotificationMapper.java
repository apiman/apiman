package io.apiman.manager.api.notifications.mappers;

import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.notifications.NotificationEntity;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.events.EventFactory;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import org.mapstruct.Mapper;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Mapper(componentModel = "cdi")
public abstract class NotificationMapper {
    @Inject EventFactory eventFactory;

    public abstract NotificationDto entityToDto(NotificationEntity entity);

    public IVersionedApimanEvent translatePayloadJsonToPojo(JsonNode event) {
        return eventFactory.toEventPojo(event);
    }
}
