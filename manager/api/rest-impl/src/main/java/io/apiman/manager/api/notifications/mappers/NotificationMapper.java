package io.apiman.manager.api.notifications.mappers;

import io.apiman.manager.api.beans.events.IVersionedApimanEvent;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.idm.UserMapper;
import io.apiman.manager.api.beans.notifications.NotificationEntity;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.events.EventFactory;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;

import java.util.Optional;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Mapper(componentModel = "cdi")
public abstract class NotificationMapper implements DataAccessUtilMixin {
    @Inject EventFactory eventFactory;
    @Inject IStorage storage;

    @ObjectFactory
    public NotificationDto<?> createDto() {
        return new NotificationDto();
    }

    public abstract NotificationDto<?> entityToDto(NotificationEntity entity);

    public <P extends IVersionedApimanEvent> P translatePayloadJsonToPojo(JsonNode event) {
        return eventFactory.toEventPojo(event);
    }

    public UserDto translateUsernameToUserDto(String username) {
        return Optional.ofNullable(tryAction(() -> storage.getUser(username)))
                       .map(UserMapper::toDto)
                       .orElse(new UserDto().setId("external-or-deleted-user"));
    }
}
