package io.apiman.manager.api.beans.apis.dto;

import io.apiman.manager.api.beans.apis.UpdateApiVersionBean;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
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
}
