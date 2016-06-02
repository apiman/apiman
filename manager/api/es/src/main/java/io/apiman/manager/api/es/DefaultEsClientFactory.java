/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.manager.api.es;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.client.config.HttpClientConfig.Builder;

import java.util.Map;

/**
 * A default implementation of the ES client factory.
 * @author eric.wittmann@gmail.com
 */
public class DefaultEsClientFactory implements IEsClientFactory {
    
    private final Map<String, String> config;
    
    /**
     * Constructor.
     * @param config
     */
    public DefaultEsClientFactory(Map<String, String> config) {
        this.config = config;
    }

    /**
     * @return the config
     */
    protected Map<String, String> getConfig() {
        return config;
    }
    
    /**
     * @see io.apiman.manager.api.es.IEsClientFactory#createClient()
     */
    @Override
    public JestClient createClient() {
        String protocol = config.get("protocol"); //$NON-NLS-1$
        if (protocol == null) {
            protocol = "http"; //$NON-NLS-1$
        }
        String host = config.get("host"); //$NON-NLS-1$
        String port = config.get("port"); //$NON-NLS-1$
        
        StringBuilder builder = new StringBuilder();
        builder.append(protocol);
        builder.append("://"); //$NON-NLS-1$
        builder.append(host);
        builder.append(":"); //$NON-NLS-1$
        builder.append(port);
        String connectionUrl = builder.toString();
        
        Builder httpConfig = new HttpClientConfig.Builder(connectionUrl).multiThreaded(true);
        updateHttpConfig(httpConfig);
        
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(httpConfig.build());
        updateJestClientFactory(factory);
        
        return factory.getObject();
    }

    /**
     * @param httpConfig
     */
    protected void updateHttpConfig(Builder httpConfig) {
        String username = config.get("username"); //$NON-NLS-1$
        String password = config.get("password"); //$NON-NLS-1$
        String timeout = config.get("timeout"); //$NON-NLS-1$
        if (username != null) {
            httpConfig.defaultCredentials(username, password);
        }
        if (timeout == null) {
            timeout = "10000"; //$NON-NLS-1$
        }
        
        int t = new Integer(timeout);
        httpConfig.connTimeout(t);
        httpConfig.readTimeout(t);
        httpConfig.maxTotalConnection(75);
        httpConfig.defaultMaxTotalConnectionPerRoute(75);
        httpConfig.multiThreaded(true);
    }

    /**
     * @param factory
     */
    protected void updateJestClientFactory(JestClientFactory factory) {
    }

}
