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
package org.overlord.apiman.dt.api.beans.audit.data;

import java.io.Serializable;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * The data saved along with the audit entry when a new version of an
 * entity is created.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class EntityVersionCreatedData implements Serializable {
    
    private static final long serialVersionUID = -8591318431623507761L;
    
    private String version;

    /**
     * Constructor.
     */
    public EntityVersionCreatedData() {
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
}
