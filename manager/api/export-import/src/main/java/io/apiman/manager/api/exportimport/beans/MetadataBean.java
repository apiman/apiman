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
package io.apiman.manager.api.exportimport.beans;

import org.joda.time.DateTime;

/**
 * A bean that holds basic information about a single export of 
 * the apiman data.  This bean includes useful information about
 * the export, including the version of apiman and the date the
 * export was performed.
 */
public class MetadataBean {
    private DateTime exportedOn;
    private String apimanVersion;

    public MetadataBean() {
    }

    /**
     * @return the exportedOn
     */
    public DateTime getExportedOn() {
        return exportedOn;
    }

    /**
     * @param exportedOn the exportedOn to set
     */
    public void setExportedOn(DateTime exportedOn) {
        this.exportedOn = exportedOn;
    }

    /**
     * @return the apimanVersion
     */
    public String getApimanVersion() {
        return apimanVersion;
    }

    /**
     * @param apimanVersion the apimanVersion to set
     */
    public void setApimanVersion(String apimanVersion) {
        this.apimanVersion = apimanVersion;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "MetadataBean [exportedOn=" + exportedOn + ", apimanVersion=" + apimanVersion + "]";
    }
}
