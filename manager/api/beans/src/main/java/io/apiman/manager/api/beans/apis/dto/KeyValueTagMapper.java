/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
