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
package io.apiman.manager.api.beans.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Models a set of beans returned as a result of a search.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class SearchResultsBean<T> implements Serializable {

    private static final long serialVersionUID = -1672829715471947181L;
    
    private List<T> beans = new ArrayList<T>();
    private int totalSize;

    /**
     * Constructor.
     */
    public SearchResultsBean() {
    }

    /**
     * @return the beans
     */
    public List<T> getBeans() {
        return beans;
    }

    /**
     * @param beans the beans to set
     */
    public void setBeans(List<T> beans) {
        this.beans = beans;
    }

    /**
     * @return the totalSize
     */
    public int getTotalSize() {
        return totalSize;
    }

    /**
     * @param totalSize the totalSize to set
     */
    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }
    
}
