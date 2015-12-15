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

import com.unboundid.ldap.sdk.ResultCode;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class LdapResult implements ILdapResult {
    public static final ILdapResult SUCCESS = new LdapResult(LdapResultCode.SUCCESS, ""); //$NON-NLS-1$
    private LdapResultCode resultCode;
    private String message;

    public LdapResult(LdapResultCode resultCode, String message) {
        this.resultCode = resultCode;
        this.message = message;
    }

    public LdapResult(ResultCode resultCode, String message) {
        this.resultCode = DefaultLdapResultCodeFactory.convertResultCode(resultCode);
        this.message = message;
    }

    @Override
    public LdapResultCode getResultCode() {
        return resultCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
