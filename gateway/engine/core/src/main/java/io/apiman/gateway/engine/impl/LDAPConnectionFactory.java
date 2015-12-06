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

package io.apiman.gateway.engine.impl;

import javax.net.ssl.SSLSocketFactory;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class LDAPConnectionFactory {
    public static LDAPConnection build(SSLSocketFactory socketFactory, String scheme, String host, int port) throws LDAPException {
        if (isLdaps(scheme)){
            return new LDAPConnection(socketFactory, host, port);
        } else {
            return new LDAPConnection(host, port);
        }
    }

    public static LDAPConnection build(SSLSocketFactory socketFactory, String scheme, String host, int port, String bindDn, String bindPassword) throws LDAPException {
        if (isLdaps(scheme)){
            return new LDAPConnection(socketFactory, host, port, bindDn, bindPassword);
        } else {
            return new LDAPConnection(host, port);
        }
    }

    private static boolean isLdaps(String scheme) {
        return scheme.toLowerCase().startsWith("ldaps"); //$NON-NLS-1$
    }
}
