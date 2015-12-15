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

package io.apiman.gateway.engine.components.ldap.result;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class DefaultExceptionFactory {

    /**
     * Convert LDAP Exceptions
     *
     * @param e the LDAP Exception
     * @return an apiman LdapExeption
     */
    public static LdapException create(LDAPException e) {
        return new LdapException(DefaultLdapResultCodeFactory.convertResultCode(e.getResultCode()),
                e.getDiagnosticMessage(), e);
    }

    public static LdapException create(ResultCode resultCode, String message) {
        return new LdapException(DefaultLdapResultCodeFactory.convertResultCode(resultCode), message, null);
    }
}
