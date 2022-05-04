package io.apiman.manager.api.beans.summary.mappers;

import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.apis.dto.ApiBeanDto;
import io.apiman.manager.api.beans.idm.DiscoverabilityEntity;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionSummaryBean;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Mapper
public interface ApiMapper {
    ApiMapper INSTANCE = Mappers.getMapper(ApiMapper.class);

    @Mappings({
        @Mapping(source = "organization.id", target = "organizationId"),
        @Mapping(source = "organization.name", target = "organizationName")

    })
    ApiSummaryBean toSummary(ApiBean apiBean);
    List<ApiSummaryBean> toSummary(List<ApiBean> apiBean);

    @Mappings({
            @Mapping(source = "api.organization.id", target = "organizationId"),
            @Mapping(source = "api.organization.name", target = "organizationName"),
            @Mapping(source = "api.id", target = "id"),
            @Mapping(source = "api.name", target = "name"),
            @Mapping(source = "api.description", target = "description"),
            @Mapping(source = "api.tags", target = "apiTags"),
            @Mapping(source = "discoverability", target = "publicDiscoverability")
    })
    ApiVersionSummaryBean toSummary(ApiVersionBean av);

    ApiBeanDto toDto(ApiBean apiBean);
}
