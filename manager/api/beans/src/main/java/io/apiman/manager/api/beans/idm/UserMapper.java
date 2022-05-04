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
