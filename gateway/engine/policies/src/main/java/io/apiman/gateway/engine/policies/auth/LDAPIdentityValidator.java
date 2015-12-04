/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.engine.policies.auth;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.ILdapComponent;
import io.apiman.gateway.engine.components.ldap.ILdapAttribute;
import io.apiman.gateway.engine.components.ldap.ILdapClientConnection;
import io.apiman.gateway.engine.components.ldap.ILdapDn;
import io.apiman.gateway.engine.components.ldap.ILdapRdn;
import io.apiman.gateway.engine.components.ldap.ILdapSearchEntry;
import io.apiman.gateway.engine.components.ldap.LdapConfigBean;
import io.apiman.gateway.engine.components.ldap.LdapSearchScope;
import io.apiman.gateway.engine.policies.AuthorizationPolicy;
import io.apiman.gateway.engine.policies.config.basicauth.LDAPBindAsType;
import io.apiman.gateway.engine.policies.config.basicauth.LDAPIdentitySource;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.lang.text.StrSubstitutor;

/**
 * An identity validator that uses the static information in the config
 * to validate the user.
 *
 * @author eric.wittmann@redhat.com
 */
public class LDAPIdentityValidator implements IIdentityValidator<LDAPIdentitySource> {

    /**
     * Constructor.
     */
    public LDAPIdentityValidator() {
    }

    private <T, Q> IAsyncResultHandler<T> successHandler(final IAsyncResultHandler<Q> errorHandler,
            final IAsyncHandler<T> successHandler) {
        return new IAsyncResultHandler<T>() {

            @Override
            public void handle(IAsyncResult<T> result) {
                if (result.isError()) {
                    errorHandler.handle(AsyncResultImpl.<Q>create(result.getError()));
                } else {
                    successHandler.handle(result.getResult());
                }
            }
        };
    }

    /**
     * @see io.apiman.gateway.engine.policies.auth.IIdentityValidator#validate(java.lang.String, java.lang.String, io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void validate(final String username, final String password, final ServiceRequest request, final IPolicyContext context,
            final LDAPIdentitySource config, final IAsyncResultHandler<Boolean> handler) {
        try {
            final ILdapComponent ldapComponent = context.getComponent(ILdapComponent.class);

            String bindDn = formatDn(config.getDnPattern(), username, request);
            String bindDnPwd = password;

            final LdapConfigBean ldapConfigBean = new LdapConfigBean();
            ldapConfigBean.setBindDn(bindDn);
            ldapConfigBean.setBindPassword(bindDnPwd);
            ldapConfigBean.setHost(config.getUri().getHost());
            ldapConfigBean.setPort(config.getUri().getPort());
            ldapConfigBean.setScheme(config.getUri().getScheme());

            // Bind as one account, search for other.
            if (config.getBindAs() == LDAPBindAsType.ServiceAccount) {
                ldapConfigBean.setBindDn(formatDn(config.getDnPattern(), config.getCredentials().getUsername(), request));
                ldapConfigBean.setBindPassword(config.getCredentials().getPassword());

                ldapComponent.connect(ldapConfigBean, successHandler(handler, new IAsyncHandler<ILdapClientConnection>() {

                    @Override
                    public void handle(ILdapClientConnection connection) {
                        String searchBaseDN = formatDn(config.getUserSearch().getBaseDn(), username, request);
                        String searchExpr = formatDn(config.getUserSearch().getExpression(), username, request);

                        connection.search(searchBaseDN, searchExpr, LdapSearchScope.SUBTREE,
                                successHandler(handler, new IAsyncHandler<List<ILdapSearchEntry>>() {

                                    @Override
                                    public void handle(List<ILdapSearchEntry> searchEntries) {
                                        if (searchEntries.size() > 1) {
                                            NamingException ex = new NamingException("Found multiple entries for the same username: " + username); //$NON-NLS-1$
                                            handler.handle(AsyncResultImpl.<Boolean>create(ex));
                                        } else if (searchEntries.isEmpty()) {
                                            handler.handle(AsyncResultImpl.create(Boolean.FALSE));
                                        } else { // Just one result
                                            String userDn = searchEntries.get(0).getDn(); // First entry
                                            if (userDn != null) {
                                                ldapConfigBean.setBindDn(userDn);
                                                ldapConfigBean.setBindPassword(password);
                                                bind(config, ldapConfigBean, ldapComponent, context, handler);
                                            } else {
                                                handler.handle(AsyncResultImpl.create(Boolean.FALSE));
                                            }
                                        }
                                    }
                                }));
                    }
                }));
            } else {
                bind(config, ldapConfigBean, ldapComponent, context, handler);
            }
        } catch (Throwable e) {
            handler.handle(AsyncResultImpl.create(e, Boolean.class));
        }
    }

    private void bind(final LDAPIdentitySource config, final LdapConfigBean ldapConfigBean, final ILdapComponent ldapComponent,
            final IPolicyContext context, final IAsyncResultHandler<Boolean> handler) {
        // If no role extraction is needed, just do a fast and simple BIND & exit
        if (!config.isExtractRoles()) {
            ldapComponent.bind(ldapConfigBean, handler);
        } else { // Otherwise open up longer-lived connection and query role info.
            ldapComponent.connect(ldapConfigBean, successHandler(handler, new IAsyncHandler<ILdapClientConnection>() {
                @Override // Extract the roles.
                public void handle(ILdapClientConnection connection) {
                    extractRoles(connection, ldapConfigBean.getBindDn(), config, context, new IAsyncResultHandler<Void>() {

                        @Override
                        public void handle(IAsyncResult<Void> result) {
                            if (result.isSuccess()) {
                                handler.handle(AsyncResultImpl.create(Boolean.TRUE));
                            } else {
                                handler.handle(AsyncResultImpl.create(Boolean.FALSE));
                            }
                        }
                    });
                }
            }));
        }
    }

    private void extractRoles(final ILdapClientConnection connection, final String userDn, final LDAPIdentitySource config,
            final IPolicyContext context, final IAsyncResultHandler<Void> resultHandler) {
        final Set<String> roles = new HashSet<>();
        // TODO test whether this is indeed the default filter that java uses.
        connection.search(userDn, "(objectClass=*)", LdapSearchScope.SUBTREE, successHandler(resultHandler, //$NON-NLS-1$
                new IAsyncHandler<List<ILdapSearchEntry>>() {

            @Override
            public void handle(List<ILdapSearchEntry> result) {
                // Look through all results (usually should only be 1)
                for (ILdapSearchEntry searchResult : result) {
                    // Get membership attribute (if any)
                    List<ILdapAttribute> attrs = searchResult.getAttributes();

                    try { // Look through all attrs - grab relevant RDNS, for each attribute (e.g. cn)
                        for (ILdapAttribute attr : attrs) {
                            if (attr.getBaseName().equals(config.getMembershipAttribute())) {
                                addRoles(attr);
                            }
                        }
                    } catch (Exception e) { // Potentially invalid RDN format
                        resultHandler.handle(AsyncResultImpl.<Void>create(e));
                    }
                    resultHandler.handle(AsyncResultImpl.create((Void) null));
                }
            }

            private void addRoles(ILdapAttribute attr) {
                // Treat value as an RDN
                for (ILdapDn dn : attr.getValuesAsDn()) {
                    for (ILdapRdn rdns : dn.getRdns()) {
                        if (rdns.hasAttribute(config.getRolenameAttribute())) {
                            for (String value : rdns.getAttributeValues()) {
                                roles.add(value);
                            }
                        }
                    }
                }
            }
        }));

        context.setAttribute(AuthorizationPolicy.AUTHENTICATED_USER_ROLES, roles);
    }

    /**
     * Formats the configured DN by replacing any properties it finds.
     * @param dnPattern
     * @param username
     * @param request
     */
    private String formatDn(String dnPattern, String username, ServiceRequest request) {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.putAll(request.getHeaders());
        valuesMap.put("username", username); //$NON-NLS-1$
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        return sub.replace(dnPattern);
    }
}
