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
 * A DN is a whole distinguished name, which comprises of RDN pairs
 * concatenated together with comma delimiters
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface ILdapDn {
    /**
     * @return get the distinguished name (DN) as a string
     */
    String getDn();

    /**
     * @return get a list of all RDNS (entries in DN).
     */
    List<ILdapRdn> getRdns();
}
