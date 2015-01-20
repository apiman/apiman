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
package io.apiman.manager.api.beans.summary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Models the entire API registry for a single application version.  This is typically
 * used to get a list of all APIs that can be consumed by a single version of a single
 * application.  Most importantly it includes the live endpoint information and API
 * keys for all of the app's service contracts/APIs.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
@XmlRootElement(name = "apis")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApiRegistryBean implements Serializable {

    private static final long serialVersionUID = 7369169626368271089L;
    
    @XmlElement(name = "api")
    private List<ApiEntryBean> apis = new ArrayList<ApiEntryBean>();

    /**
     * Constructor.
     */
    public ApiRegistryBean() {
    }

    /**
     * @return the apis
     */
    public List<ApiEntryBean> getApis() {
        return apis;
    }

    /**
     * @param apis the apis to set
     */
    public void setApis(List<ApiEntryBean> apis) {
        this.apis = apis;
    }
}
