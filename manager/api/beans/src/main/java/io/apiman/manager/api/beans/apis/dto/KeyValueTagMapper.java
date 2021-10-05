package io.apiman.manager.api.beans.apis.dto;

import io.apiman.manager.api.beans.apis.KeyValueTag;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Mapper
public interface KeyValueTagMapper {
    KeyValueTagMapper INSTANCE = Mappers.getMapper(KeyValueTagMapper.class);

    KeyValueTagDto toDto(KeyValueTag entity);

    Set<KeyValueTagDto> toDto(Set<KeyValueTag> entity);

    @Mapping(target = "id", ignore = true)
    KeyValueTag toEntity(KeyValueTagDto tag);

    Set<KeyValueTag> toEntity(Set<KeyValueTagDto> tags);
}
