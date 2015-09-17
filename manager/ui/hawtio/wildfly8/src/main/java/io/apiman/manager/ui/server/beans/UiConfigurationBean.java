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
package io.apiman.manager.ui.server.beans;


/**
 * Contains information about the UI.
 *
 * @author eric.wittmann@redhat.com
 */
public class UiConfigurationBean {
    
    private Boolean header;
    private Boolean metrics;
    
    /**
     * Constructor.
     */
    public UiConfigurationBean() {
    }

    /**
     * @return the header
     */
    public Boolean getHeader() {
        return header;
    }

    /**
     * @param header the header to set
     */
    public void setHeader(Boolean header) {
        this.header = header;
    }

    /**
     * @return the metrics
     */
    public Boolean getMetrics() {
        return metrics;
    }

    /**
     * @param metrics the metrics to set
     */
    public void setMetrics(Boolean metrics) {
        this.metrics = metrics;
    }

}
