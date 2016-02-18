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

import io.apiman.gateway.engine.components.ldap.ILdapResult;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;

/**
 * An factory for creating {@link LdapException}s from various exceptions and
 * status objects returned by the default LDAP implementation's backend.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class DefaultExceptionFactory {

    private DefaultExceptionFactory() {
    }

    /**
     * Convert from {@link LDAPException} to apiman's {@link LdapException}
     *
     * @param e the LDAP Exception
     * @return a new LdapException
     */
    public static LdapException create(LDAPException e) {
        return new LdapException(DefaultLdapResultCodeFactory.convertResultCode(e.getResultCode()),
                e.getDiagnosticMessage(), e);
    }

    /**
     * Create an {@link LdapException} from an {@link LdapResultCode} and message
     *
     * @param resultCode the result code
     * @param message the exception message
     * @return a new LDAPException
     */
    public static LdapException create(LdapResultCode resultCode, String message) {
        return new LdapException(resultCode, message, null);
    }

    /**
     * Create an {@link LdapException} from a {@link ResultCode} and message
     *
     * @param resultCode the result code
     * @param message the exception message
     * @return a new LDAPException
     */
    public static LdapException create(ResultCode resultCode, String message) {
        return new LdapException(DefaultLdapResultCodeFactory.convertResultCode(resultCode), message, null);
    }

    /**
     * Create an {@link LdapException} from an {@link ILdapResult}
     *
     * @param result the result code
     * @return a new LDAPException
     */
    public static LdapException create(ILdapResult result) {
        return new LdapException(result.getResultCode(), result.getMessage(), null);
    }
}
