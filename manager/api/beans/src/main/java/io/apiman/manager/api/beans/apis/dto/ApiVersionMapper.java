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

    @Mappings({
            @Mapping(source = "apb.version", target = "version"),
            @Mapping(source = "avb", target = "apiVersion"),
            @Mapping(source = "apb.discoverability", target = "discoverability")
    })
    ApiPlanBean fromDto(ApiPlanBeanDto apb, ApiVersionBean avb);

    @Mappings({
            @Mapping(source = "apb.version", target = "version"),
            @Mapping(source = "avb", target = "apiVersion"),
            @Mapping(source = "apb.discoverability", target = "discoverability", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    })
    ApiPlanBean fromDto(UpdateApiPlanDto apb, ApiVersionBean avb);


    void merge(ApiPlanBean source, @MappingTarget ApiPlanBean target);

    @Mapping(target = "publicDiscoverability", source = "discoverability")
    ApiVersionBeanDto toDto(ApiVersionBean avb);

    @Mapping(target = "publicDiscoverability", source = "dl", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    ApiVersionBeanDto toDto(ApiVersionBean avb, DiscoverabilityLevel dl);
}