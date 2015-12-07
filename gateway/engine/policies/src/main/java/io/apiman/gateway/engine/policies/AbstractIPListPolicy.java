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
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.policies.config.IPListConfig;


/**
 * Base class for the ip whitelist and blacklist policies.
 *
 * @author eric.wittmann@redhat.com
 * @param <C> the config type
 */
public abstract class AbstractIPListPolicy<C> extends AbstractMappedPolicy<C> {

    /**
     * Gets the remote address for comparison.
     * @param request the request
     * @param config the config
     */
    protected String getRemoteAddr(ApiRequest request, IPListConfig config) {
        String httpHeader = config.getHttpHeader();
        if (httpHeader != null && httpHeader.trim().length() > 0) {
            String value = (String) request.getHeaders().get(httpHeader);
            if (value != null) {
                return value;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Returns true if the remote address is a match for the configured
     * values in the IP List.
     * @param config the config
     * @param remoteAddr the remote address
     */
    protected boolean isMatch(IPListConfig config, String remoteAddr) {
        if (config.getIpList().contains(remoteAddr)) {
            return true;
        }
        try {
            String [] remoteAddrSplit = remoteAddr.split("\\."); //$NON-NLS-1$
            for (String ip : config.getIpList()) {
                String [] ipSplit = ip.split("\\."); //$NON-NLS-1$
                if (remoteAddrSplit.length == ipSplit.length) {
                    int numParts = ipSplit.length;
                    boolean matches = true;
                    for (int idx = 0; idx < numParts; idx++) {
                        if (ipSplit[idx].equals("*") || ipSplit[idx].equals(remoteAddrSplit[idx])) { //$NON-NLS-1$
                            // This component matches!
                        } else {
                            matches = false;
                            break;
                        }
                    }
                    if (matches) {
                        return true;
                    }
                }
            }
        } catch (Throwable t) {
            // eat it
        }
        return false;
    }

}
