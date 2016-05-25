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
package io.apiman.manager.api.core.catalog;

import io.apiman.manager.api.beans.summary.ApiCatalogBean;
import io.apiman.manager.api.beans.summary.ApiNamespaceBean;
import io.apiman.manager.api.beans.summary.AvailableApiBean;
import io.apiman.manager.api.core.IApiCatalog;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An API catalog that gets its data from a simple JSON file.
 *
 * @author eric.wittmann@redhat.com
 */
public class JsonApiCatalog implements IApiCatalog {

    private static final ObjectMapper mapper = new ObjectMapper();

    private URI catalogUri;
    private List<AvailableApiBean> apis;

    /**
     * Constructor.
     * @param config
     */
    public JsonApiCatalog(Map<String, String> config) {
        String cu = config.get("catalog-url"); //$NON-NLS-1$
        try {
            cu = cu.trim();
            if (cu.startsWith("file:")) { //$NON-NLS-1$
                cu = cu.replace('\\', '/');
            }
            catalogUri = new URI(cu);
        } catch (Exception e) {
            throw new RuntimeException("Error configuring the JSON API catalog from a URI: " + cu, e); //$NON-NLS-1$
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IApiCatalog#search(java.lang.String, java.lang.String)
     */
    @Override
    public List<AvailableApiBean> search(String keyword, String namespace) {
        if (apis == null) {
            apis = loadAPIs(catalogUri);
        }
        ArrayList<AvailableApiBean> rval = new ArrayList<>();

        for (AvailableApiBean api : apis) {
            if ("*".equals(keyword) || api.getName().toLowerCase().contains(keyword.toLowerCase())) { //$NON-NLS-1$
                rval.add(api);
            }
        }

        return rval;
    }
    
    /**
     * @see io.apiman.manager.api.core.IApiCatalog#getNamespaces(java.lang.String)
     */
    @Override
    public List<ApiNamespaceBean> getNamespaces(String currentUser) {
        return Collections.EMPTY_LIST;
    }

    /**
     * @param uri the URL to load the catalog from
     * @return Loads APIs from the catalog URL
     */
    private static List<AvailableApiBean> loadAPIs(URI uri) {
        try {
            ApiCatalogBean catalog = (ApiCatalogBean) mapper.reader(ApiCatalogBean.class).readValue(uri.toURL());
            return catalog.getApis();
        } catch (Exception e) {
            throw new RuntimeException("Error loading APIs from a URL: " + uri, e); //$NON-NLS-1$
        }
    }

}
