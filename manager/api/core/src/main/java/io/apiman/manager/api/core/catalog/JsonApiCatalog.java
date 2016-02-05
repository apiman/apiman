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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.manager.api.beans.summary.ApiCatalogBean;
import io.apiman.manager.api.beans.summary.AvailableApiBean;
import io.apiman.manager.api.core.IApiCatalog;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An API catalog that gets its data from a simple JSON file.
 *
 * @author eric.wittmann@redhat.com
 */
public class JsonApiCatalog implements IApiCatalog {

    private static final ObjectMapper mapper = new ObjectMapper();

    private URL catalogUrl;
    private List<AvailableApiBean> apis;

    /**
     * Constructor.
     * @param config
     */
    public JsonApiCatalog(Map<String, String> config) {
        String cu = config.get("catalog-url"); //$NON-NLS-1$
        try {
            catalogUrl = new URL(cu);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IApiCatalog#search(java.lang.String)
     */
    @Override
    public List<AvailableApiBean> search(String keyword) {
        if (apis == null) {
            apis = loadAPIs(catalogUrl);
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
     * @param catalogUrl the URL to load the catalog from
     * @return Loads APIs from the catalog URL
     */
    private static List<AvailableApiBean> loadAPIs(URL catalogUrl) {
        try {
            ApiCatalogBean catalog = (ApiCatalogBean) mapper.reader(ApiCatalogBean.class).readValue(catalogUrl);
            return catalog.getApis();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
