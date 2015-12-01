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

import io.apiman.gateway.engine.components.ldap.ILdapRdn;

import java.util.Arrays;
import java.util.List;

import com.unboundid.ldap.sdk.RDN;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class DefaultLdapRdn implements ILdapRdn {

    private RDN rdn;
    private List<String> attributeNamesCache;
    private List<String> attributeValuesCache;

    public DefaultLdapRdn(RDN rdn) {
        this.rdn = rdn;
    }

    @Override
    public List<String> getAttributeNames() {
        if (attributeNamesCache == null) {
            attributeNamesCache = Arrays.asList(rdn.getAttributeNames());
        }
        return attributeNamesCache;
    }

    @Override
    public List<String> getAttributeValues() {
        if (attributeValuesCache == null) {
            attributeValuesCache = Arrays.asList(rdn.getAttributeValues());
        }
        return attributeValuesCache;
    }

    @Override
    public boolean hasAttribute(String name) {
        return rdn.hasAttribute(name);
    }
}
