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

import io.apiman.manager.api.beans.apis.ApiPlanBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Map API Version Update
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ApiVersionUpdateMapper {
    ApiVersionUpdateMapper INSTANCE = Mappers.getMapper(ApiVersionUpdateMapper.class);

    Set<UpdateApiPlanDto> toDto(Set<ApiPlanBean> plans);

    @Mappings({
            @Mapping(source = "apb.version", target = "version"),
            @Mapping(source = "avb", target = "apiVersion"),
            @Mapping(source = "apb.discoverability", target = "discoverability"),
    })
    ApiPlanBean fromDto(ApiPlanBeanDto apb, ApiVersionBean avb);

    @Mappings({
            @Mapping(source = "apb.version", target = "version"),
            @Mapping(source = "avb", target = "apiVersion"),
            @Mapping(source = "apb.discoverability", target = "discoverability"),
    })
    ApiPlanBean fromDto(UpdateApiPlanDto apb, ApiVersionBean avb);

    void merge(ApiPlanBean source, @MappingTarget ApiPlanBean target);
}