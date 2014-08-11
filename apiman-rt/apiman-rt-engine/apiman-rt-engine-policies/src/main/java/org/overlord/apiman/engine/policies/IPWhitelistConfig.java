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
package org.overlord.apiman.engine.policies;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration object for the IP whitelist policy.
 *
 * @author eric.wittmann@redhat.com
 */
public class IPWhitelistConfig {
    
    private Set<String> ipList = new HashSet<String>();
    
    /**
     * Constructor.
     */
    public IPWhitelistConfig() {
    }

    /**
     * @return the ipList
     */
    public Set<String> getIpList() {
        return ipList;
    }

    /**
     * @param ipList the ipList to set
     */
    public void setIpList(Set<String> ipList) {
        this.ipList = ipList;
    }

}
