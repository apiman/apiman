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

import java.util.List;

/**
 * An RDN is a key(s) value(s) entry in a RN
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface ILdapRdn {
    /**
     * @return the attribute names from the RDN.
     */
    List<String> getAttributeNames();

    /**
     * @return the attribute value(s) from the RDN
     */
    List<String> getAttributeValues();

    /**
     * If RDN has attribute
     *
     * @param name the attribute name
     * @return true if has attribute; else, false.
     */
    boolean hasAttribute(String name);
}
