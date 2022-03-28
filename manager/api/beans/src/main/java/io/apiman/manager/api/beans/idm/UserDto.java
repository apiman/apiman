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
import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO of {@link UserBean}.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    @NotBlank
    private String username;
    @NotBlank
    private String fullName;
    @NotBlank
    private String email;
    @NotNull
    private Locale locale = Locale.getDefault();
    @Nullable
    private Boolean admin;

    public UserDto() {
    }

    public String getUsername() {
        return username;
    }

    public UserDto setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getFullName() {
        return fullName;
    }

    public UserDto setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public Locale getLocale() {
        return locale;
    }

    public UserDto setLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public boolean isAdmin() {
        return admin;
    }

    public UserDto setAdmin(boolean admin) {
        this.admin = admin;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDto userDto = (UserDto) o;
        return Objects.equals(username, userDto.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserDto.class.getSimpleName() + "[", "]")
                .add("username='" + username + "'")
                .add("fullName='" + fullName + "'")
                .add("email='" + email + "'")
                .add("locale=" + locale)
                .add("admin=" + admin)
                .toString();
    }
}
