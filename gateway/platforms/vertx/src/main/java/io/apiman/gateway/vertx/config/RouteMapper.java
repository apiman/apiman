/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.vertx.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.vertx.java.core.json.JsonObject;

/**
 * Maps simple http path to bus address.
 * 
 * Example: '/gateway -> vertx.apiman.gateway'
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class RouteMapper {
    private Map<String, Integer> routeMap = new HashMap<>();
    
    public RouteMapper() {}
    
    public RouteMapper(JsonObject routeMappings) {        
        for(String routeName : routeMappings.getFieldNames()) {
            routeMap.put(routeName, routeMappings.getInteger(routeName));
        }
    }
    
    public Map<String, Integer> getRoutes() {
        return routeMap;
    }
    
    public Integer getAddress(String path) {
        return routeMap.get(firstPathElem(path));
    }
    
    public boolean hasRoute(String path) {
        return getAddress(path) != null;
    }   
    
    /**
     * Simplistic & fast mapping of first element of path, avoiding any regex for now.
     * For instance /gateway/a/b/c => gateway
     */
    protected String firstPathElem(String path) {
        return StringUtils.split(path, "/", 2)[0]; //$NON-NLS-1$
    }
}
