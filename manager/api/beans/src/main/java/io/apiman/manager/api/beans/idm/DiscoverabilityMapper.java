package io.apiman.manager.api.beans.idm;

import java.util.Collection;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */

@Mapper
public interface DiscoverabilityMapper {
    DiscoverabilityMapper INSTANCE = Mappers.getMapper(DiscoverabilityMapper.class);
    DiscoverabilityDto toDto(DiscoverabilityEntity de);
    Collection<DiscoverabilityDto> toDto(Collection<DiscoverabilityEntity> de);
}
