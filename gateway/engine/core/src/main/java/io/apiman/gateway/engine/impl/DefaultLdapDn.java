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

import io.apiman.gateway.engine.components.ldap.ILdapDn;
import io.apiman.gateway.engine.components.ldap.ILdapRdn;

import java.util.ArrayList;
import java.util.List;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.RDN;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class DefaultLdapDn implements ILdapDn {

    private DN dn;
    private List<ILdapRdn> cache;

    public DefaultLdapDn(DN dn) {
        this.dn = dn;
    }

    public DefaultLdapDn(String dn) throws LDAPException {
        this.dn = new DN(dn);
    }

    @Override
    public String getDn() {
        return dn.getRDNString();
    }

    @Override
    public List<ILdapRdn> getRdns() {
        if (cache == null) {
            cache = new ArrayList<>(dn.getRDNs().length);
            for (RDN rdn : dn.getRDNs()) {
                cache.add(new DefaultLdapRdn(rdn));
            }
        }
        return cache;
    }
}
