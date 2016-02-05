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
package io.apiman.manager.api.beans.summary;

import java.io.Serializable;

/**
 * Returns managed endpoint information.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiVersionEndpointSummaryBean implements Serializable {

    private static final long serialVersionUID = -4655383228161917800L;

    private String managedEndpoint;

    /**
     * Constructor.
     */
    public ApiVersionEndpointSummaryBean() {
    }

    /**
     * @return the managedEndpoint
     */
    public String getManagedEndpoint() {
        return managedEndpoint;
    }

    /**
     * @param managedEndpoint the managedEndpoint to set
     */
    public void setManagedEndpoint(String managedEndpoint) {
        this.managedEndpoint = managedEndpoint;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "ApiVersionEndpointSummaryBean [managedEndpoint=" + managedEndpoint + "]";
    }

}
