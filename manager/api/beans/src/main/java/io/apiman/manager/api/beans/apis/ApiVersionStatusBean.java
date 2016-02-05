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

package io.apiman.manager.api.beans.apis;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple status bean for a version of an API.  This bean includes
 * information about the list of tasks that must be completed before an
 * API version will move to the Ready state, indicating that it can
 * be published.
 *
 * @author eric.wittmann@redhat.com
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ApiVersionStatusBean {

    private ApiStatus status;
    private List<StatusItemBean> items = new ArrayList<>();

    /**
     * Constructor.
     */
    public ApiVersionStatusBean() {
    }

    /**
     * @return the items
     */
    public List<StatusItemBean> getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(List<StatusItemBean> items) {
        this.items = items;
    }

    /**
     * @return the status
     */
    public ApiStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(ApiStatus status) {
        this.status = status;
    }

}
