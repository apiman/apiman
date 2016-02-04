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
package io.apiman.manager.api.beans.clients;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

/**
 * Bean used when creating a new client version.
 *
 * @author eric.wittmann@redhat.com
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class NewClientVersionBean implements Serializable {

    private static final long serialVersionUID = 960818800225855945L;

    private String version;
    private boolean clone;
    private String cloneVersion;

    /**
     * Constructor.
     */
    public NewClientVersionBean() {
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

    /**
     * @return the clone
     */
    public boolean isClone() {
        return clone;
    }

    /**
     * @param clone the clone to set
     */
    public void setClone(boolean clone) {
        this.clone = clone;
    }

    /**
     * @return the cloneVersion
     */
    public String getCloneVersion() {
        return cloneVersion;
    }

    /**
     * @param cloneVersion the cloneVersion to set
     */
    public void setCloneVersion(String cloneVersion) {
        this.cloneVersion = cloneVersion;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "NewClientVersionBean [version=" + version + ", clone=" + clone + ", cloneVersion="
                + cloneVersion + "]";
    }

}
