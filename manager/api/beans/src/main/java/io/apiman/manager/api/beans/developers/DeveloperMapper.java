package io.apiman.manager.api.beans.developers;

import io.apiman.manager.api.beans.apis.ApiVersionBean;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Mapper
public interface DeveloperMapper {

    DeveloperMapper INSTANCE = Mappers.getMapper(DeveloperMapper.class);

    DeveloperApiVersionBeanDto toDto(ApiVersionBean apiVersion);
}
