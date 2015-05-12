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
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.policies.AuthorizationPolicy;
import io.apiman.gateway.engine.policies.config.basicauth.LDAPBindAsType;
import io.apiman.gateway.engine.policies.config.basicauth.LDAPIdentitySource;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

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

    /**
     * @see io.apiman.gateway.engine.policies.auth.IIdentityValidator#validate(java.lang.String, java.lang.String, io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void validate(String username, String password, ServiceRequest request, IPolicyContext context,
            LDAPIdentitySource config, IAsyncResultHandler<Boolean> handler) {
        String url = config.getUrl();
        String bindDN = formatDn(config.getDnPattern(), username, request);
        String bindDNPwd = password;

        if (config.getBindAs() == LDAPBindAsType.ServiceAccount) {
            bindDN = formatDn(config.getDnPattern(), config.getCredentials().getUsername(), request);
            bindDNPwd = config.getCredentials().getPassword();
        }

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory"); //$NON-NLS-1$
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.REFERRAL, "follow"); //$NON-NLS-1$
        if (url.startsWith("ldaps")) { //$NON-NLS-1$
            // TODO handle connections to ldaps with self-sign certs?
        }
        env.put(Context.SECURITY_AUTHENTICATION, "simple"); //$NON-NLS-1$
        env.put(Context.SECURITY_PRINCIPAL, bindDN);
        env.put(Context.SECURITY_CREDENTIALS, bindDNPwd);
        try {
            InitialDirContext dirContext = new InitialDirContext(env);

            // If we're using a service account to search for the actual UserDN,
            // then do the search and, if found, rebind.  Otherwise just return true.
            if (config.getBindAs() == LDAPBindAsType.ServiceAccount) {
                validateAsServiceAccount(username, password, request, context, config, handler, dirContext);
            } else {
                if (config.isExtractRoles()) {
                    extractRoles(dirContext, bindDN, config, context);
                }
                handler.handle(AsyncResultImpl.create(Boolean.TRUE));
            }
        } catch (AuthenticationException e) {
            handler.handle(AsyncResultImpl.create(Boolean.FALSE));
        } catch (NamingException e) {
            handler.handle(AsyncResultImpl.create(e, Boolean.class));
        }
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
    private void validateAsServiceAccount(String username, String password, ServiceRequest request,
            IPolicyContext context, LDAPIdentitySource config, IAsyncResultHandler<Boolean> handler,
            DirContext dirContext) throws NamingException {

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBaseDN = formatDn(config.getUserSearch().getBaseDn(), username, request);
        String searchExpr = formatDn(config.getUserSearch().getExpression(), username, request);
        NamingEnumeration<SearchResult> result = dirContext.search(searchBaseDN, searchExpr, controls);
        if (result == null || !result.hasMore()) {
            handler.handle(AsyncResultImpl.create(Boolean.FALSE));
        } else {
            SearchResult element = result.nextElement();
            String userDN = element.getNameInNamespace();
            // Multiple results?
            if (result.hasMore()) {
                throw new NamingException("Found multiple entries for the same username: " + username); //$NON-NLS-1$
            }

            // We have the userDN and the password - rebind now
            String url = config.getUrl();
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory"); //$NON-NLS-1$
            env.put(Context.PROVIDER_URL, url);
            env.put(Context.SECURITY_AUTHENTICATION, "simple"); //$NON-NLS-1$
            env.put(Context.SECURITY_PRINCIPAL, userDN);
            env.put(Context.SECURITY_CREDENTIALS, password);
            new InitialDirContext(env);
            if (config.isExtractRoles()) {
                extractRoles(dirContext, userDN, config, context);
            }
            handler.handle(AsyncResultImpl.create(Boolean.TRUE));
        }
    }

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
    private void extractRoles(DirContext dirContext, String userDN, LDAPIdentitySource config,
            IPolicyContext context) throws NamingException {
        Set<String> roles = new HashSet<>();

        Attributes attributes = dirContext.getAttributes(userDN);
        if (attributes == null) {
            return;
        }
        Attribute attribute = attributes.get(config.getMembershipAttribute());
        if (attribute != null) {
            NamingEnumeration<?> all = attribute.getAll();
            while (all.hasMoreElements()) {
                String groupDN = (String) all.nextElement();
                try {
                    Attributes groupAttrs = dirContext.getAttributes(groupDN);
                    String roleName = (String) groupAttrs.get(config.getRolenameAttribute()).get();
                    roles.add(roleName);
                } catch (NamingException ne) {
                    // skip and move on to the next one
                }
            }
        }

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
