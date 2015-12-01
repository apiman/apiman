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
                    System.err.println("IS ERROR");
                    result.getError().printStackTrace();
                    errorHandler.handle(AsyncResultImpl.create(result.getError()));
                } else {
                    System.err.println("IS RESULT");
                    successHandler.handle(result.getResult());
                }
            }
        };
    }

    /**
     * @see io.apiman.gateway.engine.policies.auth.IIdentityValidator#validate(java.lang.String, java.lang.String, io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void validate(String username, String password, ServiceRequest request, IPolicyContext context,
            LDAPIdentitySource config, IAsyncResultHandler<Boolean> handler) {
        try {
            ILdapComponent ldapComponent = context.getComponent(ILdapComponent.class);

            String bindDn = formatDn(config.getDnPattern(), username, request);
            String bindDnPwd = password;//config.getCredentials().getPassword();

            LdapConfigBean ldapConfigBean = new LdapConfigBean();
            ldapConfigBean.setBindDn(bindDn);
            ldapConfigBean.setBindPassword(bindDnPwd);
            ldapConfigBean.setHost(config.getUri().getHost());
            ldapConfigBean.setPort(config.getUri().getPort());

            // Bind as one account, search for other.
            if (config.getBindAs() == LDAPBindAsType.ServiceAccount) {
              ldapConfigBean.setBindDn(formatDn(config.getDnPattern(), config.getCredentials().getUsername(), request));
              ldapConfigBean.setBindPassword(config.getCredentials().getPassword());
            }

            if (config.getBindAs() == LDAPBindAsType.ServiceAccount) {
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
                                            NamingException ex = new NamingException("Found multiple entries for the same username: " + username);
                                            handler.handle(AsyncResultImpl.create(ex));
                                        } else if (searchEntries.isEmpty()) {
                                            handler.handle(AsyncResultImpl.create(Boolean.FALSE));
                                        } else { // Just one result
                                            ILdapSearchEntry entry = searchEntries.get(0); // First entry
                                            ILdapAttribute attr = entry.getAttributes().get(0); // First attr?
                                            String userDn = attr.getBaseName(); // TODO Basename or full name? TODO this seems weird, should it not be value?

                                            if (userDn != null) {
                                                ldapConfigBean.setBindDn(userDn);
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
            System.err.println("ERROR!!!!!");
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }


//        String url = config.getUrl();
//        String bindDN = formatDn(config.getDnPattern(), username, request);
//        String bindDNPwd = password;
//
//        if (config.getBindAs() == LDAPBindAsType.ServiceAccount) {
//            bindDN = formatDn(config.getDnPattern(), config.getCredentials().getUsername(), request);
//            bindDNPwd = config.getCredentials().getPassword();
//        }
//
//        Hashtable<String, String> env = new Hashtable<>();
//        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory"); //$NON-NLS-1$
//        env.put(Context.PROVIDER_URL, url);
//        env.put(Context.REFERRAL, "follow"); //$NON-NLS-1$
//        if (url.startsWith("ldaps")) { //$NON-NLS-1$
//            // TODO handle connections to ldaps with self-sign certs?
//        }
//        env.put(Context.SECURITY_AUTHENTICATION, "simple"); //$NON-NLS-1$
//        env.put(Context.SECURITY_PRINCIPAL, bindDN);
//        env.put(Context.SECURITY_CREDENTIALS, bindDNPwd);
//        try {
//            InitialDirContext dirContext = new InitialDirContext(env);
//
//            // If we're using a service account to search for the actual UserDN,
//            // then do the search and, if found, rebind.  Otherwise just return true.
//            if (config.getBindAs() == LDAPBindAsType.ServiceAccount) {
//                validateAsServiceAccount(username, password, request, context, config, handler, dirContext);
//            } else {
//                if (config.isExtractRoles()) {
//                    extractRoles(dirContext, bindDN, config, context);
//                }
//                handler.handle(AsyncResultImpl.create(Boolean.TRUE));
//            }
//        } catch (AuthenticationException e) {
//            handler.handle(AsyncResultImpl.create(Boolean.FALSE));
//        } catch (NamingException e) {
//            handler.handle(AsyncResultImpl.create(e, Boolean.class));
//        }
    }

    private void bind(LDAPIdentitySource config, LdapConfigBean ldapConfigBean, ILdapComponent ldapComponent, IPolicyContext context, IAsyncResultHandler<Boolean> handler) {
        // If no role extraction is needed, just do a fast and simple BIND & exit
        if (!config.isExtractRoles()) {
            ldapComponent.bind(ldapConfigBean, handler);
        } else { // Otherwise open up longer-lived connection and query role info.
            ldapComponent.connect(ldapConfigBean, successHandler(handler, new IAsyncHandler<ILdapClientConnection>() {

                @Override // Extract the roles. TODO distinguish between error and just unauthorized?
                public void handle(ILdapClientConnection connection) { //errors should be sent upwards i think.
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
        return false;
    }

    /**
     * Validate by searching for the user node in LDAP, then (if found) rebinding
     * using the provided password.
     * @param username
     * @param password
     * @param request
     * @param context
     * @param config
     * @param handler
     * @param dirContext
     * @throws NamingException
     */
//    private void validateAsServiceAccount(String username, String password, ServiceRequest request,
//            IPolicyContext context, LDAPIdentitySource config, IAsyncResultHandler<Boolean> handler,
//            DirContext dirContext) throws NamingException {
//
//        SearchControls controls = new SearchControls();
//        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//        String searchBaseDN = formatDn(config.getUserSearch().getBaseDn(), username, request);
//        String searchExpr = formatDn(config.getUserSearch().getExpression(), username, request);
//        NamingEnumeration<SearchResult> result = dirContext.search(searchBaseDN, searchExpr, controls);
//        if (result == null || !result.hasMore()) {
//            handler.handle(AsyncResultImpl.create(Boolean.FALSE));
//        } else {
//            SearchResult element = result.nextElement();
//            String userDN = element.getNameInNamespace();
//            // Multiple results?
//            if (result.hasMore()) {
//                throw new NamingException("Found multiple entries for the same username: " + username); //$NON-NLS-1$
//            }
//
//            // We have the userDN and the password - rebind now
//            String url = config.getUrl();
//            Hashtable<String, String> env = new Hashtable<>();
//            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory"); //$NON-NLS-1$
//            env.put(Context.PROVIDER_URL, url);
//            env.put(Context.SECURITY_AUTHENTICATION, "simple"); //$NON-NLS-1$
//            env.put(Context.SECURITY_PRINCIPAL, userDN);
//            env.put(Context.SECURITY_CREDENTIALS, password);
//            new InitialDirContext(env);
//            if (config.isExtractRoles()) {
//                extractRoles(dirContext, userDN, config, context);
//            }
//            handler.handle(AsyncResultImpl.create(Boolean.TRUE));
//        }
//    }

    /**
     * Extracts the roles from the LDAP directory.  This is done by getting the
     * LDAP user node located at 'userDN' and then extracting all of its LDAP
     * group memberships.  Typically this is done by going through all of the
     * "memberof" attributes for the User Node.
     * @param dirContext
     * @param userDN
     * @param config
     * @param context
     * @throws NamingException
     */
//    private void extractRoles(DirContext dirContext, String userDN, LDAPIdentitySource config,
//            IPolicyContext context) throws NamingException {
//        Set<String> roles = new HashSet<>();
//
//
//
//        Attributes attributes = dirContext.getAttributes(userDN);
//        if (attributes == null) {
//            return;
//        }
//        Attribute attribute = attributes.get(config.getMembershipAttribute());
//        if (attribute != null) {
//            NamingEnumeration<?> all = attribute.getAll();
//            while (all.hasMoreElements()) {
//                String groupDN = (String) all.nextElement();
//                try {
//                    Attributes groupAttrs = dirContext.getAttributes(groupDN);
//                    String roleName = (String) groupAttrs.get(config.getRolenameAttribute()).get();
//                    roles.add(roleName);
//                } catch (NamingException ne) {
//                    // skip and move on to the next one
//                }
//            }
//        }
//
//        context.setAttribute(AuthorizationPolicy.AUTHENTICATED_USER_ROLES, roles);
//    }

    private void extractRoles(ILdapClientConnection connection, String userDn, LDAPIdentitySource config,
            IPolicyContext context, IAsyncResultHandler<Void> resultHandler) {
        Set<String> roles = new HashSet<>();
        // TODO test whether this is indeed the default filter that java uses.
        connection.search(userDn, "(objectClass=*)", LdapSearchScope.SUBTREE, successHandler(resultHandler,
                new IAsyncHandler<List<ILdapSearchEntry>>() {

            @Override
            public void handle(List<ILdapSearchEntry> result) {
              for (ILdapSearchEntry searchResult : result) {
                  for(ILdapAttribute attr : searchResult.getAttributes()) {
                      if (attr.getBaseName().equals(config.getRolenameAttribute())) {
                          roles.add(attr.getAsString());
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
