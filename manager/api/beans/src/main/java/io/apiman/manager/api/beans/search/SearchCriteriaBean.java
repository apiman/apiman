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
import java.util.StringJoiner;

/**
 * Generic search criteria used when searching for beans.
 *
 * @author eric.wittmann@redhat.com
 */
public class SearchCriteriaBean implements Serializable {

    private static final long serialVersionUID = 5103776179000907112L;

    protected List<SearchCriteriaFilterBean> filters = new ArrayList<>();
    protected OrderByBean orderBy;
    protected PagingBean paging;

    /**
     * Constructor.
     */
    public SearchCriteriaBean() {
    }

    public SearchCriteriaBean(SearchCriteriaBean bean) {
        this.filters = bean.getFilters();
        this.orderBy = bean.getOrderBy();
        this.paging = bean.getPaging();
    }

    /**
     * Adds a single filter to the criteria.
     * @param name the filter name
     * @param value the filter value
     * @param operator the operator type
     */
    public SearchCriteriaBean addFilter(String name, String value, SearchCriteriaFilterOperator operator) {
        SearchCriteriaFilterBean filter = new SearchCriteriaFilterBean();
        filter.setName(name);
        filter.setValue(value);
        filter.setOperator(operator);
        filters.add(filter);
        return this;
    }

    /**
     * @param page the page
     */
    public SearchCriteriaBean setPage(int page) {
        if (this.paging == null)
            this.paging = new PagingBean();
        getPaging().setPage(page);
        return this;
    }

    /**
     * @param pageSize size of page
     */
    public SearchCriteriaBean setPageSize(int pageSize) {
        if (this.paging == null)
            this.paging = new PagingBean();
        getPaging().setPageSize(pageSize);
        return this;
    }

    /**
     * @param name the name
     * @param ascending whether is ascending
     */
    public SearchCriteriaBean setOrder(String name, boolean ascending) {
        if (this.orderBy == null)
            this.orderBy = new OrderByBean();
        orderBy.setName(name);
        orderBy.setAscending(ascending);
        return this;
    }

    /**
     * @return the filters
     */
    public List<SearchCriteriaFilterBean> getFilters() {
        return filters;
    }

    /**
     * @param filters the filters to set
     */
    public SearchCriteriaBean setFilters(List<SearchCriteriaFilterBean> filters) {
        this.filters = filters;
        return this;
    }

    /**
     * @return the paging
     */
    public PagingBean getPaging() {
        return paging;
    }

    /**
     * @param paging the paging to set
     */
    public SearchCriteriaBean setPaging(PagingBean paging) {
        this.paging = paging;
        return this;
    }

    /**
     * @return the orderBy
     */
    public OrderByBean getOrderBy() {
        return orderBy;
    }

    /**
     * @param orderBy the orderBy to set
     */
    public SearchCriteriaBean setOrderBy(OrderByBean orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filters == null) ? 0 : filters.hashCode());
        result = prime * result + ((orderBy == null) ? 0 : orderBy.hashCode());
        result = prime * result + ((paging == null) ? 0 : paging.hashCode());
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
        SearchCriteriaBean other = (SearchCriteriaBean) obj;
        if (filters == null) {
            if (other.filters != null)
                return false;
        } else if (!filters.equals(other.filters))
            return false;
        if (orderBy == null) {
            if (other.orderBy != null)
                return false;
        } else if (!orderBy.equals(other.orderBy))
            return false;
        if (paging == null) {
            return other.paging == null;
        } else {
            return paging.equals(other.paging);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SearchCriteriaBean.class.getSimpleName() + "[", "]")
                       .add("filters=" + filters)
                       .add("orderBy=" + orderBy)
                       .add("paging=" + paging)
                       .toString();
    }
}
