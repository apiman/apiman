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

/**
 * Various LDAP response codes
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public enum LdapResultCode {
    SUCCESS,

    INVALID_CREDENTIALS,

    AUTHORIZATION_DENIED,

    INAPPROPRIATE_AUTHENTICATION,

    INSUFFICIENT_ACCESS_RIGHTS,

    AUTH_METHOD_NOT_SUPPORTED,

    STRONG_AUTH_REQUIRED,

    AUTH_UNKNOWN,

    OPERATIONS_ERROR,

    TIME_LIMIT_EXCEEDED,

    SIZE_LIMIT_EXCEEDED,

    COMPARE_FALSE,

    COMPARE_TRUE,

    REFERRAL,

    ADMIN_LIMIT_EXCEEDED,

    UNAVAILABLE_CRITICAL_EXTENSION,

    CONFIDENTIALITY_REQUIRED,

    SASL_BIND_IN_PROGRESS,

    NO_SUCH_ATTRIBUTE,

    UNDEFINED_ATTRIBUTE_TYPE,

    INAPPROPRIATE_MATCHING,

    CONSTRAINT_VIOLATION,

    ATTRIBUTE_OR_VALUE_EXISTS,

    INVALID_ATTRIBUTE_SYNTAX,

    NO_SUCH_OBJECT,

    INVALID_DN_SYNTAX,

    ALIAS_DEREFERENCING_PROBLEM,

    BUSY,

    UNAVAILABLE,

    UNWILLING_TO_PERFORM,

    SERVER_DOWN,

    LOCAL_ERROR,

    ENCODING_ERROR,

    DECODING_ERROR,

    TIMEOUT,

    FILTER_ERROR,

    USER_CANCELED,

    PARAM_ERROR,

    NO_MEMORY,

    CONNECT_ERROR,

    NOT_SUPPORTED,

    PROTOCOL_ERROR,

    OTHER_FAILURE;

    public static boolean isSuccess(LdapResultCode code) {
        return code.equals(SUCCESS);
    }

    public static boolean isAuthFailure(LdapResultCode code) {
       return code.equals(INVALID_CREDENTIALS) ||
              code.equals(AUTHORIZATION_DENIED) ||
              code.equals(INAPPROPRIATE_AUTHENTICATION);
    }
}
