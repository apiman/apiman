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
package io.apiman.gateway.engine.components.ldap;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/**
 * Simple model of an LDAP Attribute
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface ILdapAttribute {
    /**
     * The name plus attribute options
     * @return the description
     */
    String getDescription();

    /**
     * Just name, no attribute options
     * @return the base name
     */
    String getBaseName();

    /**
     * Treat the value as a DN (and in turn access RDNs)
     * @return the value as DN
     */
    ILdapDn getValueAsDn();

    /**
     * Are there options attached?
     * @return true if has options, else false
     */
    boolean hasOptions();

    /**
     * Get options associated with this attribute
     * @return the options
     */
    Set<String> getOptions();

    /**
     * @return as string representation
     */
    String getAsString();

    /**
     * @return as byte[] representation
     */
    byte[] getAsByteArray();

    /**
     * @return as Boolean representation
     */
    Boolean getAsBoolean();

    /**
     * @return as OffsetDateTime representation
     */
    OffsetDateTime getValueAsDateTime();

    /**
     * @return as Integer representation
     */
    Integer getValueAsInteger();

    /**
     * @return as Long representation
     */
    Long getValueAsLong();

    /**
     * @return as String[] representation
     */
    String[] getValuesAsStringArray();

    /**
     * @return as byte[][] representation
     */
    byte[][] getValuesAsByteArrays();

    /**
     * @return as list of DN when multiple entries
     */
    List<ILdapDn> getValuesAsDn();
}
