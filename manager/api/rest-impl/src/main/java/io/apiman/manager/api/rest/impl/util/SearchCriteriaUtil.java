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
package io.apiman.manager.api.rest.impl.util;

import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.rest.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.exceptions.i18n.Messages;

import java.util.HashSet;
import java.util.Set;

/**
 * Some utility methods related to searches and search criteria.
 *
 * @author eric.wittmann@redhat.com
 */
public final class SearchCriteriaUtil {
    
    protected static final Set<SearchCriteriaFilterOperator> validOperators = new HashSet<>();
    static {
        validOperators.add(SearchCriteriaFilterOperator.eq);
        validOperators.add(SearchCriteriaFilterOperator.gt);
        validOperators.add(SearchCriteriaFilterOperator.gte);
        validOperators.add(SearchCriteriaFilterOperator.lt);
        validOperators.add(SearchCriteriaFilterOperator.lte);
        validOperators.add(SearchCriteriaFilterOperator.neq);
        validOperators.add(SearchCriteriaFilterOperator.like);
    }

    private SearchCriteriaUtil() {
    }

    /**
     * Validates that the search criteria bean is complete and makes sense.
     * @param criteria the search criteria
     * @throws InvalidSearchCriteriaException when the search criteria is not valid
     */
    public static void validateSearchCriteria(SearchCriteriaBean criteria) throws InvalidSearchCriteriaException {
        if (criteria.getPaging() != null) {
            if (criteria.getPaging().getPage() < 1) {
                throw new InvalidSearchCriteriaException(Messages.i18n.format("SearchCriteriaUtil.MissingPage")); //$NON-NLS-1$
            }
            if (criteria.getPaging().getPageSize() < 1) {
                throw new InvalidSearchCriteriaException(Messages.i18n.format("SearchCriteriaUtil.MissingPageSize")); //$NON-NLS-1$
            }
        }
        int count = 1;
        for (SearchCriteriaFilterBean filter : criteria.getFilters()) {
            if (filter.getName() == null || filter.getName().trim().length() == 0) {
                throw new InvalidSearchCriteriaException(Messages.i18n.format("SearchCriteriaUtil.MissingSearchFilterName", count)); //$NON-NLS-1$
            }
            if (filter.getValue() == null || filter.getValue().trim().length() == 0) {
                throw new InvalidSearchCriteriaException(Messages.i18n.format("SearchCriteriaUtil.MissingSearchFilterValue", count)); //$NON-NLS-1$
            }
            if (filter.getOperator() == null || !validOperators.contains(filter.getOperator())) {
                throw new InvalidSearchCriteriaException(Messages.i18n.format("SearchCriteriaUtil.MissingSearchFilterOperator", count)); //$NON-NLS-1$
            }
            count++;
        }
        if (criteria.getOrderBy() != null && (criteria.getOrderBy().getName() == null || criteria.getOrderBy().getName().trim().length() == 0)) {
            throw new InvalidSearchCriteriaException(Messages.i18n.format("SearchCriteriaUtil.MissingOrderByName")); //$NON-NLS-1$
        }
    }
}
