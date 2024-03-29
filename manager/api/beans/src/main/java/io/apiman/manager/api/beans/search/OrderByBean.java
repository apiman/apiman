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
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Models order-by for a search.
 *
 * @author eric.wittmann@redhat.com
 */
public class OrderByBean implements Serializable {

    private static final long serialVersionUID = -7569401325900866820L;

    private boolean ascending;
    private String name;

    public OrderByBean(boolean ascending, String name) {
        this.ascending = ascending;
        this.name = name;
    }
    /**
     * Constructor.
     */
    public OrderByBean() {
    }

    /**
     * @return the ascending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * @param ascending the ascending to set
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
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
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderByBean that = (OrderByBean) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OrderByBean.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("ascending=" + ascending)
                .toString();
    }
}
