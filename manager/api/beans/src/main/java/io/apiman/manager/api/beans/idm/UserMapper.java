package io.apiman.manager.api.beans.idm;

import java.util.Locale;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto toDto(UserBean userBean);

    default Locale localeConvert(String languageTag) {
        if (languageTag == null) {
            return null;
        }
        return new Locale.Builder().setLanguageTag(languageTag).build();
    }

    default String localeConvert(Locale locale) {
        if (locale == null) {
            return null;
        }
        return locale.toLanguageTag();
    }
}
