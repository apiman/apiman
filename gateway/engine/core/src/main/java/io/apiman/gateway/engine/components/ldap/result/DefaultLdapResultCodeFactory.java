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

import com.unboundid.ldap.sdk.ResultCode;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class DefaultLdapResultCodeFactory {

    public static LdapResultCode convertResultCode(ResultCode resultCode) {
        switch (resultCode.intValue()) {
        case ResultCode.SUCCESS_INT_VALUE:
            return LdapResultCode.SUCCESS;
        case ResultCode.OPERATIONS_ERROR_INT_VALUE:
            return LdapResultCode.OPERATIONS_ERROR;
        case ResultCode.TIME_LIMIT_EXCEEDED_INT_VALUE:
            return LdapResultCode.TIME_LIMIT_EXCEEDED;
        case ResultCode.SIZE_LIMIT_EXCEEDED_INT_VALUE:
            return LdapResultCode.SIZE_LIMIT_EXCEEDED;
        case ResultCode.COMPARE_FALSE_INT_VALUE:
            return LdapResultCode.COMPARE_FALSE;
        case ResultCode.COMPARE_TRUE_INT_VALUE:
            return LdapResultCode.COMPARE_TRUE;
        case ResultCode.AUTH_METHOD_NOT_SUPPORTED_INT_VALUE:
            return LdapResultCode.AUTH_METHOD_NOT_SUPPORTED;
        case ResultCode.STRONG_AUTH_REQUIRED_INT_VALUE:
            return LdapResultCode.STRONG_AUTH_REQUIRED;
        case ResultCode.REFERRAL_INT_VALUE:
            return LdapResultCode.REFERRAL;
        case ResultCode.ADMIN_LIMIT_EXCEEDED_INT_VALUE:
            return LdapResultCode.ADMIN_LIMIT_EXCEEDED;
        case ResultCode.UNAVAILABLE_CRITICAL_EXTENSION_INT_VALUE:
            return LdapResultCode.UNAVAILABLE_CRITICAL_EXTENSION;
        case ResultCode.CONFIDENTIALITY_REQUIRED_INT_VALUE:
            return LdapResultCode.CONFIDENTIALITY_REQUIRED;
        case ResultCode.SASL_BIND_IN_PROGRESS_INT_VALUE:
            return LdapResultCode.SASL_BIND_IN_PROGRESS;
        case ResultCode.NO_SUCH_ATTRIBUTE_INT_VALUE:
            return LdapResultCode.NO_SUCH_ATTRIBUTE;
        case ResultCode.UNDEFINED_ATTRIBUTE_TYPE_INT_VALUE:
            return LdapResultCode.UNDEFINED_ATTRIBUTE_TYPE;
        case ResultCode.INAPPROPRIATE_MATCHING_INT_VALUE:
            return LdapResultCode.INAPPROPRIATE_MATCHING;
        case ResultCode.CONSTRAINT_VIOLATION_INT_VALUE:
            return LdapResultCode.CONSTRAINT_VIOLATION;
        case ResultCode.ATTRIBUTE_OR_VALUE_EXISTS_INT_VALUE:
            return LdapResultCode.ATTRIBUTE_OR_VALUE_EXISTS;
        case ResultCode.INVALID_ATTRIBUTE_SYNTAX_INT_VALUE:
            return LdapResultCode.INVALID_ATTRIBUTE_SYNTAX;
        case ResultCode.NO_SUCH_OBJECT_INT_VALUE:
            return LdapResultCode.NO_SUCH_OBJECT;
        case ResultCode.INVALID_DN_SYNTAX_INT_VALUE:
            return LdapResultCode.INVALID_DN_SYNTAX;
        case ResultCode.ALIAS_DEREFERENCING_PROBLEM_INT_VALUE:
            return LdapResultCode.ALIAS_DEREFERENCING_PROBLEM;
        case ResultCode.INAPPROPRIATE_AUTHENTICATION_INT_VALUE:
            return LdapResultCode.INAPPROPRIATE_AUTHENTICATION;
        case ResultCode.INVALID_CREDENTIALS_INT_VALUE:
            return LdapResultCode.INVALID_CREDENTIALS;
        case ResultCode.INSUFFICIENT_ACCESS_RIGHTS_INT_VALUE:
            return LdapResultCode.INSUFFICIENT_ACCESS_RIGHTS;
        case ResultCode.BUSY_INT_VALUE:
            return LdapResultCode.BUSY;
        case ResultCode.UNAVAILABLE_INT_VALUE:
            return LdapResultCode.UNAVAILABLE;
        case ResultCode.UNWILLING_TO_PERFORM_INT_VALUE:
            return LdapResultCode.UNWILLING_TO_PERFORM;
        case ResultCode.SERVER_DOWN_INT_VALUE:
            return LdapResultCode.SERVER_DOWN;
        case ResultCode.LOCAL_ERROR_INT_VALUE:
            return LdapResultCode.LOCAL_ERROR;
        case ResultCode.ENCODING_ERROR_INT_VALUE:
            return LdapResultCode.ENCODING_ERROR;
        case ResultCode.DECODING_ERROR_INT_VALUE:
            return LdapResultCode.DECODING_ERROR;
        case ResultCode.TIMEOUT_INT_VALUE:
            return LdapResultCode.TIMEOUT;
        case ResultCode.AUTH_UNKNOWN_INT_VALUE:
            return LdapResultCode.AUTH_UNKNOWN;
        case ResultCode.FILTER_ERROR_INT_VALUE:
            return LdapResultCode.FILTER_ERROR;
        case ResultCode.USER_CANCELED_INT_VALUE:
            return LdapResultCode.USER_CANCELED;
        case ResultCode.PARAM_ERROR_INT_VALUE:
            return LdapResultCode.PARAM_ERROR;
        case ResultCode.NO_MEMORY_INT_VALUE:
            return LdapResultCode.NO_MEMORY;
        case ResultCode.CONNECT_ERROR_INT_VALUE:
            return LdapResultCode.CONNECT_ERROR;
        case ResultCode.NOT_SUPPORTED_INT_VALUE:
            return LdapResultCode.NOT_SUPPORTED;
        case ResultCode.AUTHORIZATION_DENIED_INT_VALUE:
            return LdapResultCode.AUTHORIZATION_DENIED;
        case ResultCode.PROTOCOL_ERROR_INT_VALUE:
            return LdapResultCode.PROTOCOL_ERROR;
        default:
            return LdapResultCode.OTHER_FAILURE;
        }
    }
}
