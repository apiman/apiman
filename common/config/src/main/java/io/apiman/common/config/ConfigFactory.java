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
package io.apiman.common.config;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;

/**
 * Factory for creating a configuration object from apiman.properties.
 *
 * @author eric.wittmann@redhat.com
 */
public class ConfigFactory {

    static {
        ConfigurationInterpolator.registerGlobalLookup("env", new EnvLookup()); //$NON-NLS-1$
        ConfigurationInterpolator.registerGlobalLookup("crypt", new CryptLookup()); //$NON-NLS-1$
        ConfigurationInterpolator.registerGlobalLookup("vault", new VaultLookup()); //$NON-NLS-1$
    }

    public static final CompositeConfiguration createConfig() {
        CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
        compositeConfiguration.addConfiguration(new SystemPropertiesConfiguration());
        compositeConfiguration.addConfiguration(ConfigFileConfiguration.create("apiman.properties", "apiman.config.url")); //$NON-NLS-1$ //$NON-NLS-2$
        return compositeConfiguration;
    }

}
