/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.security.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.idm.UserDto;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.LocaleUtils;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;

/**
 * An alternative security context used when protected by keycloak.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped @Alternative
public class KeycloakSecurityContext extends AbstractSecurityContext {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(KeycloakSecurityContext.class);
    private volatile KeycloakAdminClient keycloakAdminClient;

    /**
     * Constructor.
     */
    public KeycloakSecurityContext() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentUser() {
        return servletRequest.get().getRemoteUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullName() {
        HttpServletRequest request = servletRequest.get();
        org.keycloak.KeycloakSecurityContext session = (org.keycloak.KeycloakSecurityContext) request.getAttribute(org.keycloak.KeycloakSecurityContext.class.getName());
        if (session != null) {
            return session.getToken().getName();
        } else {
            return null;
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmail() {
        HttpServletRequest request = servletRequest.get();
        org.keycloak.KeycloakSecurityContext session = (org.keycloak.KeycloakSecurityContext) request.getAttribute(org.keycloak.KeycloakSecurityContext.class.getName());
        if (session != null) {
            return session.getToken().getEmail();
        } else {
            return null;
        }
    }

    @Override
    public Locale getLocale() {
        HttpServletRequest request = servletRequest.get();
        org.keycloak.KeycloakSecurityContext session = (org.keycloak.KeycloakSecurityContext) request.getAttribute(org.keycloak.KeycloakSecurityContext.class.getName());
        if (session != null) {
            return Optional.ofNullable(LocaleUtils.toLocale(session.getToken().getLocale()))
                    .orElse(request.getLocale());
        } else {
            return null;
        }
    }

    @Override
    public List<UserDto> getRemoteUsersWithRole(String roleName) {
        List<UserDto> keycloakUsersWithRole = getKeycloakAdminClient().getUsersForRole(roleName);
        LOGGER.debug("Keycloak users for role {0} (using same realm as configured): {2}", roleName, keycloakUsersWithRole);
        return keycloakUsersWithRole;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserDto> getUsersWithRole(String roleName, String orgName) {
        List<UserDto> apimanUsers = super.getUsersWithRole(roleName, orgName);
        LOGGER.debug("Apiman stored users for role {0} and org {1}: {2}", roleName, orgName, apimanUsers);
        // join lists, distinct as there is likely be overlap
        return Stream.concat(apimanUsers.stream(), getRemoteUsersWithRole(roleName).stream())
                     .distinct()
                     .collect(Collectors.toUnmodifiableList());
    }

    private KeycloakAdminClient getKeycloakAdminClient() {
        if (keycloakAdminClient == null) {
            synchronized (this) {
                HttpServletRequest request = servletRequest.get();
                RefreshableKeycloakSecurityContext session = (RefreshableKeycloakSecurityContext) request.getAttribute(org.keycloak.KeycloakSecurityContext.class.getName());
                keycloakAdminClient = new KeycloakAdminClient(session.getDeployment());
                return keycloakAdminClient;
            }
        }
        return keycloakAdminClient;
    }
}
