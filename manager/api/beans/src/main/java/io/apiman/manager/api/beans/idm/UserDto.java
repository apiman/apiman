package io.apiman.manager.api.beans.idm;

import java.util.Objects;
import java.util.StringJoiner;
import javax.validation.constraints.NotBlank;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class UserDto {
    @NotBlank
    private String id;
    @NotBlank
    private String username;
    @NotBlank
    private String fullName;
    @NotBlank
    private String email;

    public UserDto() {
    }

    public String getId() {
        return id;
    }

    public UserDto setId(String id) {
        this.id = id;
        return this;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDto userDto = (UserDto) o;
        return Objects.equals(id, userDto.id) && Objects.equals(username, userDto.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserDto.class.getSimpleName() + "[", "]")
             .add("id='" + id + "'")
             .add("username='" + username + "'")
             .add("fullName='" + fullName + "'")
             .add("email='" + email + "'")
             .toString();
    }
}
