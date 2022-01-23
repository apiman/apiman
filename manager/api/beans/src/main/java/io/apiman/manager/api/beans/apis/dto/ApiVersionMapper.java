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
import io.apiman.manager.api.beans.apis.UpdateApiVersionBean;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;

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
 * Map {@link ApiVersionBean} and {@link ApiPlanBean}.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ApiVersionMapper {
    ApiVersionMapper INSTANCE = Mappers.getMapper(ApiVersionMapper.class);

    /**
     * Strip out any fields that are not permitted for change after publication.
     */
    @Mappings({
            @Mapping(source = "endpointProperties", target = "endpointProperties"),
            @Mapping(source = "extendedDescription", target = "extendedDescription"),
            @Mapping(source = "plans", target = "plans")
    })
    UpdateApiVersionBean toPublishedUpdateBean(UpdateApiVersionBean uvb);

    Set<ApiPlanBeanDto> toDto(Set<ApiPlanBean> plans);

    Set<UpdateApiPlanDto> toDto2(Set<ApiPlanBean> plans);

    @Mappings({
            @Mapping(source = "discoverability", target = "discoverability"),
            @Mapping(source = "planId", target = "planId")
    })
    ApiPlanBeanDto toDto(ApiPlanBean plan);

    void merge(ApiPlanBean source, @MappingTarget ApiPlanBean target);

    @Mapping(target = "publicDiscoverability", source = "discoverability")
    ApiVersionBeanDto toDto(ApiVersionBean avb);

    @Mapping(target = "publicDiscoverability", source = "dl", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    ApiVersionBeanDto toDto(ApiVersionBean avb, DiscoverabilityLevel dl);
}