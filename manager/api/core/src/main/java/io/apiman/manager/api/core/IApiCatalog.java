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
package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.summary.ApiNamespaceBean;
import io.apiman.manager.api.beans.summary.AvailableApiBean;

import java.util.List;

/**
 * Represents some sort of catalog of live APIs.  This is used to lookup
 * APIs to import into apiman.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IApiCatalog {

    /**
     * Called to find available APIs that match the given search keyword.  Note that
     * the search keyword may be a partial word (for example "ech" instead of "echo").  It
     * is up to the implementation to decide how to handle partial cases.  Typically this
     * should return all APIs that contain the partial keyword, thus returning things
     * like "echo" "public-echo" and "echo-location".
     *
     * @param keyword the search keyword
     * @param namespace a namespace within which to search for available APIs (only relevant in some platforms e.g. fabric8)
     * @return the available APIs
     */
    public List<AvailableApiBean> search(String keyword, String namespace);
    
    /**
     * Called to get a list of all available namespaces in the API Catalog.
     * 
     * @param currentUser the current authenticated user
     * @return a list of all namespaces available
     */
    public List<ApiNamespaceBean> getNamespaces(String currentUser);

}
