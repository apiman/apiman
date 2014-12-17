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
package io.apiman.gateway.engine.policies.config;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Configuration object for the Ignored Resources policy.
 *
 * @author rubenrm1@gmail.com
 */
@Portable
public class IgnoredResourcesConfig {
    
    private String pathToIgnore = "";
    
    /**
     * Constructor.
     */
    public IgnoredResourcesConfig() {
    }

    /**
     * @return the pathToIgnore
     */
    public String getPathToIgnore() {
		return pathToIgnore;
	}

    /**
     * @param pathToIgnore the pathToIgnore to set
     */
    public void setPathToIgnore(String pathToIgnore) {
		this.pathToIgnore = pathToIgnore;
	}

}
