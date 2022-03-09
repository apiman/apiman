package io.apiman.manager.api.beans.apis.dto;

import io.apiman.manager.api.beans.apis.ApiPlanBean;
import io.apiman.manager.api.beans.apis.UpdateApiVersionBean;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.TargetType;
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
            @Mapping(source = "exposeInPortal", target = "exposeInPortal"),
            @Mapping(source = "plans", target = "plans")
    })
    UpdateApiVersionBean toPublishedUpdateBean(UpdateApiVersionBean uvb);

    Set<ApiPlanBeanDto> toDto(Set<ApiPlanBean> plans);

    Set<ApiPlanBean> fromDto(Set<ApiPlanBeanDto> plans);

    ApiPlanBeanDto merge(ApiPlanBeanDto source, @MappingTarget ApiPlanBeanDto target);
}
