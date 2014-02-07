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
package org.overlord.apiman.dt.api.rest.impl.util;

import java.util.HashSet;
import java.util.Set;

import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaFilterBean;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidSearchCriteriaException;

/**
 * Some utility methods related to searches and search criteria.
 *
 * @author eric.wittmann@redhat.com
 */
public final class SearchCriteriaUtil {
    
    public static final Set<String> validOperators = new HashSet<String>();
    static {
        validOperators.add(SearchCriteriaFilterBean.OPERATOR_EQ);
        validOperators.add(SearchCriteriaFilterBean.OPERATOR_GT);
        validOperators.add(SearchCriteriaFilterBean.OPERATOR_GTE);
        validOperators.add(SearchCriteriaFilterBean.OPERATOR_LT);
        validOperators.add(SearchCriteriaFilterBean.OPERATOR_LTE);
        validOperators.add(SearchCriteriaFilterBean.OPERATOR_NEQ);
        validOperators.add(SearchCriteriaFilterBean.OPERATOR_LIKE);
    }

    /**
     * Validates that the search criteria bean is complete and makes sense.
     * @param criteria
     */
    public static final void validateSearchCriteria(SearchCriteriaBean criteria) throws InvalidSearchCriteriaException {
        if (criteria.getPaging() != null) {
            if (criteria.getPaging().getPage() < 1) {
                throw new InvalidSearchCriteriaException("Missing or invalid 'page' param in search criteria.");
            }
            if (criteria.getPaging().getPageSize() < 1) {
                throw new InvalidSearchCriteriaException("Missing or invalid 'pageSize' param in search criteria.");
            }
        }
        int count = 1;
        for (SearchCriteriaFilterBean filter : criteria.getFilters()) {
            if (filter.getName() == null || filter.getName().trim().length() == 0) {
                throw new InvalidSearchCriteriaException("Missing search filter name (filter #" + count + ").");
            }
            if (filter.getValue() == null || filter.getValue().trim().length() == 0) {
                throw new InvalidSearchCriteriaException("Missing search filter value (filter #" + count + ").");
            }
            if (filter.getOperator() == null || !validOperators.contains(filter.getOperator())) {
                throw new InvalidSearchCriteriaException("Unsupported or missing search filter operator (filter #" + count + ").");
            }
            count++;
        }
        if (criteria.getOrderBy() != null && (criteria.getOrderBy().getName() == null || criteria.getOrderBy().getName().trim().length() == 0)) {
            throw new InvalidSearchCriteriaException("Missing 'name' in 'orderBy'.");
        }
    }
}
