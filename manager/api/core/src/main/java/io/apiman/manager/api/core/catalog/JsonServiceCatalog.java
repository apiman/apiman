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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import io.apiman.manager.api.beans.summary.AvailableServiceBean;
import io.apiman.manager.api.beans.summary.ServiceCatalogBean;
import io.apiman.manager.api.core.IServiceCatalog;

/**
 * A service catalog that gets its data from a simple JSON file.
 *
 * @author eric.wittmann@redhat.com
 */
public class JsonServiceCatalog implements IServiceCatalog {

    private static final ObjectMapper mapper = new ObjectMapper();

    private URL catalogUrl;
    private List<AvailableServiceBean> services;

    /**
     * Constructor.
     * @param config
     */
    public JsonServiceCatalog(Map<String, String> config) {
        String cu = config.get("catalog-url"); //$NON-NLS-1$
        try {
            catalogUrl = new URL(cu);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IServiceCatalog#search(java.lang.String)
     */
    @Override
    public List<AvailableServiceBean> search(String keyword) {
        if (services == null) {
            services = loadServices(catalogUrl);
        }
        ArrayList<AvailableServiceBean> rval = new ArrayList<>();

        for (AvailableServiceBean service : services) {
            if (service.getName().toLowerCase().contains(keyword.toLowerCase())) {
                rval.add(service);
            }
        }

        return rval;
    }

    /**
     * @param catalogUrl the URL to load the catalog from
     * @return Loads services from the catalog URL
     */
    private static List<AvailableServiceBean> loadServices(URL catalogUrl) {
        try {
            ServiceCatalogBean catalog = (ServiceCatalogBean) mapper.reader(ServiceCatalogBean.class).readValue(catalogUrl);
            return catalog.getServices();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
