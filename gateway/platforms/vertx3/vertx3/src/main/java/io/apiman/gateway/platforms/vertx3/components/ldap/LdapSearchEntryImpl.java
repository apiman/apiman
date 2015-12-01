///*
// * Copyright 2015 JBoss Inc
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package io.apiman.gateway.platforms.vertx3.components.ldap;
//
//import io.apiman.gateway.engine.components.ldap.ILdapAttribute;
//import io.apiman.gateway.engine.components.ldap.ILdapSearchEntry;
//import io.apiman.gateway.engine.impl.DefaultLdapAttribute;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import com.unboundid.ldap.sdk.SearchResultEntry;
//
///**
// * @author Marc Savy {@literal <msavy@redhat.com>}
// */
//public class LdapSearchEntryImpl implements ILdapSearchEntry {
//
//    private SearchResultEntry elem;
//
//    public LdapSearchEntryImpl(SearchResultEntry elem) {
//        this.elem = elem;
//    }
//
//    @Override // TODO caching optimisations?
//    public ILdapAttribute getAttribute(String key) {
//        return new DefaultLdapAttribute(elem.getAttribute(key));
//    }
//
//    @Override // TODO caching optimisations?
//    public List<ILdapAttribute> getAttributes() {
//        return elem.getAttributes().stream().map(elem -> { return new DefaultLdapAttribute(elem); }).collect(Collectors.toList());
//    }
//
//}
