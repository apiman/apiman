package io.apiman.manager.api.beans.idm;

import org.jetbrains.annotations.NotNull;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class UserMapper {
    public static UserDto toDto(@NotNull UserBean userBean) {
        return new UserDto()
             .setId(userBean.getUsername())
             .setUsername(userBean.getUsername())
             .setEmail(userBean.getEmail())
             .setFullName(userBean.getFullName());
    }
}
