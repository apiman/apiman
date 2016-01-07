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
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class LdapException extends Exception {
    private static final long serialVersionUID = 7864217780903440819L;
    private LdapResultCode resultCode;

    public LdapException(LdapResultCode resultCode, String message, Throwable e) {
        super(message, e);
        this.resultCode = resultCode;
    }

    public LdapResultCode getResultCode() {
        return resultCode;
    }

    @Override
    public String toString() {
        return resultCode + ": " + super.getMessage(); //$NON-NLS-1$
    }
}
