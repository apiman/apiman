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
package io.apiman.distro.es;

import io.apiman.common.config.ConfigFactory;

import org.apache.commons.configuration.CompositeConfiguration;

/**
 * Configuration for the quickstart/distro embedded ES component.
 *
 * @author eric.wittmann@redhat.com
 */
public class DistroESConfig extends CompositeConfiguration {

    /**
     * Constructor.
     */
    public DistroESConfig() {
        addConfiguration(ConfigFactory.createConfig());
    }

    public String getHttpPortRange() {
        return getString("apiman.distro-es.http-port-range", "19200-19300"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getTransportPortRange() {
        return getString("apiman.distro-es.transport-port-range", "19300-19400"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getBindHost() {
        return getString("apiman.distro-es.network.bind_host", null); //$NON-NLS-1$
    }


}
