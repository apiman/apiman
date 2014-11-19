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
package org.overlord.apiman.dt.api.beans.search;

import java.io.Serializable;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Encapsulates paging information.  Useful when listing beans or searching
 * for beans.  In these cases the criteria might match a large number of
 * beans, and we only want to return a certain number of them.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class PagingBean implements Serializable {
    
    private static final long serialVersionUID = -7218662169900773534L;
    
    private int page;
    private int pageSize;

    /**
     * Constructor.
     */
    public PagingBean() {
    }

    /**
     * @return the page
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page the page to set
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + page;
        result = prime * result + pageSize;
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PagingBean other = (PagingBean) obj;
        if (page != other.page)
            return false;
        if (pageSize != other.pageSize)
            return false;
        return true;
    }

}
