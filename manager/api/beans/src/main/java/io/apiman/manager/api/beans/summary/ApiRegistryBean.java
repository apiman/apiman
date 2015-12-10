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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models the entire API registry for a single client version.  This is typically
 * used to get a list of all APIs that can be consumed by a single version of a single
 * client.  Most importantly it includes the live endpoint information and API
 * keys for all of the client's contracts/APIs.
 *
 * @author eric.wittmann@redhat.com
 */
@XmlRootElement(name = "apiRegistry")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApiRegistryBean implements Serializable {

    private static final long serialVersionUID = 7369169626368271089L;

    private List<ApiEntryBean> apis = new ArrayList<>();

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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        final int maxLen = 10;
        return "ApiRegistryBean [apis="
                + (apis != null ? apis.subList(0, Math.min(apis.size(), maxLen)) : null) + "]";
    }
}
