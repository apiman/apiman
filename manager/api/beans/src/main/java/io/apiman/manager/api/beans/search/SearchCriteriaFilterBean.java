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

/**
 * Represents a single filter or search criteria.  This is used when searching
 * for beans.
 *
 * @author eric.wittmann@redhat.com
 */
public class SearchCriteriaFilterBean implements Serializable {

    private static final long serialVersionUID = -1199180207971619165L;

    private String name;
    private String value;
    private SearchCriteriaFilterOperator operator;

    /**
     * Constructor.
     */
    public SearchCriteriaFilterBean() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public SearchCriteriaFilterBean setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public SearchCriteriaFilterBean setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * @return the operator
     */
    public SearchCriteriaFilterOperator getOperator() {
        return operator;
    }

    /**
     * @param operator the operator to set
     */
    public SearchCriteriaFilterBean setOperator(SearchCriteriaFilterOperator operator) {
        this.operator = operator;
        return this;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((operator == null) ? 0 : operator.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        SearchCriteriaFilterBean other = (SearchCriteriaFilterBean) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (operator == null) {
            if (other.operator != null)
                return false;
        } else if (!operator.equals(other.operator))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
