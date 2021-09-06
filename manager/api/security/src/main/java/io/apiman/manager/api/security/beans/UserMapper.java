package io.apiman.manager.api.security.beans;

import io.apiman.manager.api.beans.idm.UserBean;

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
