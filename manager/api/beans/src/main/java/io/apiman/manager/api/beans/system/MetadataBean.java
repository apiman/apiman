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
package io.apiman.manager.api.beans.system;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * A bean that holds basic information about a single export of
 * the apiman data.  This bean includes useful information about
 * the export, including the version of apiman and the date the
 * export was performed.
 */
@Entity
@Table(name = "metadata")
public class MetadataBean implements Serializable {

    private static final long serialVersionUID = 6596182309560003059L;

    @Id
    @Column(updatable=false, nullable=false)
    private Long id;
    @Column(name = "exported_on", updatable=true, nullable=true)
    private Date exportedOn;
    @Column(name = "apiman_version", updatable=true, nullable=true)
    private String apimanVersion;
    @Column(name = "imported_on", updatable=true, nullable=true)
    private Date importedOn;
    @Column(name = "apiman_version_at_import", updatable=true, nullable=true)
    private String apimanVersionAtImport;
    @Column(updatable=true, nullable=true)
    private Boolean success;

    public MetadataBean() {
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
        return "MetadataBean [exportedOn=" + getExportedOn() + ", apimanVersion=" + getApimanVersion() +
                ", importedOn=" + getImportedOn() + ", apimanVersionAtImport=" + getApimanVersionAtImport() + "]";
    }

    /**
     * @return the exportedOn
     */
    public Date getExportedOn() {
        return exportedOn;
    }

    /**
     * @param exportedOn the exportedOn to set
     */
    public void setExportedOn(Date exportedOn) {
        this.exportedOn = exportedOn;
    }

    public Date getImportedOn() {
        return importedOn;
    }

    public void setImportedOn(Date importedOn) {
        this.importedOn = importedOn;
    }

    public String getApimanVersionAtImport() {
        return apimanVersionAtImport;
    }

    public void setApimanVersionAtImport(String apimanVersionAtImport) {
        this.apimanVersionAtImport = apimanVersionAtImport;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
