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

import io.apiman.gateway.engine.components.ldap.ILdapAttribute;
import io.apiman.gateway.engine.components.ldap.ILdapDn;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPException;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class DefaultLdapAttribute implements ILdapAttribute {

    private Attribute attribute;
    private List<ILdapDn> dnListCache;

    public DefaultLdapAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getDescription() {
        return attribute.getName();
    }

    @Override
    public String getBaseName() {
        return attribute.getBaseName();
    }

    @Override
    public boolean hasOptions() {
        return attribute.hasOptions();
    }

    @Override
    public Set<String> getOptions() {
        return attribute.getOptions();
    }

    @Override
    public String getAsString() {
        return attribute.getValue();
    }

    @Override
    public byte[] getAsByteArray() {
        return attribute.getValueByteArray();
    }

    @Override
    public Boolean getAsBoolean() {
        return attribute.getValueAsBoolean();
    }

    @Override
    public DateTime getValueAsDateTime() {
        return new DateTime(attribute.getValueAsDate());
    }

    @Override
    public Integer getValueAsInteger() {
        return attribute.getValueAsInteger();
    }

    @Override
    public Long getValueAsLong() {
        return attribute.getValueAsLong();
    }

    @Override
    public String[] getValuesAsStringArray() {
        return attribute.getValues();
    }

    @Override
    public byte[][] getValuesAsByteArrays() {
        return attribute.getValueByteArrays();
    }

    @Override
    public ILdapDn getValueAsDn() {
        return new DefaultLdapDn(attribute.getValueAsDN());
    }

    @Override // often there are multiple attributes with the same name - perfectly valid!
    public List<ILdapDn> getValuesAsDn() {
        if (dnListCache == null) {
            dnListCache = new ArrayList<>(attribute.getValues().length);
            for (String value : attribute.getValues()) {
                try {
                    dnListCache.add(new DefaultLdapDn(value));
                } catch (LDAPException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return dnListCache;
    }

}
