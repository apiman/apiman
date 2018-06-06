/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.manager.api.es.util;

/**
 * @author ewittman
 */
public class FilterBuilders {

    public static TermsFilterBuilder termsFilter(String name, String... values) {
        return new TermsFilterBuilder(name, values);
    }


    public static TermBuilder termFilter(String term, String value) {
        return new TermBuilder(term, value);
    }

    public static TermBuilder termFilter(String term, boolean value) {
        return new TermBuilder(term, value);
    }

    public static TermBuilder termFilter(String term, Long value) {
        return new TermBuilder(term, value);
    }

    public static BoolFilterBuilder boolFilter(QueryBuilder ... filters) {
        return new BoolFilterBuilder(filters);
    }

    public static FilterBuilder filter(QueryBuilder... filters) {
        return new FilterBuilder(filters);
    }

    public static ShouldFilterBuilder shouldFilter(QueryBuilder ... filters) {
        return new ShouldFilterBuilder(filters);
    }

    public static MustNotFilterBuilder mustNotFilter(QueryBuilder ... filters) {
        return new MustNotFilterBuilder(filters);
    }

    public static MustFilterBuilder mustFilter(QueryBuilder ... filters) {
        return new MustFilterBuilder(filters);
    }

    public static ExistsFilterBuilder existsFilter(String fieldName) {
        return new ExistsFilterBuilder(fieldName);
    }

    public static BoolFilterBuilder notExistOrFalse(String fieldName) {
        // ES 5.x has no convenient way to express this without sub-query composition.
        // https://www.elastic.co/guide/en/elasticsearch/reference/5.6/query-dsl-exists-query.html#_literal_missing_literal_query
        return FilterBuilders.boolFilter(
                // OR
                FilterBuilders.shouldFilter(
                        // NOT TERM "deleted" TRUE
                        FilterBuilders.boolFilter(
                                FilterBuilders.mustNotFilter(
                                            FilterBuilders.termFilter(fieldName, true)
                                        )
                        ),
                        // NOT EXISTS FIELD "deleted"
                        FilterBuilders.boolFilter(
                                FilterBuilders.mustNotFilter(
                                            FilterBuilders.existsFilter(fieldName)
                                        )
                        )

                )
        );
    }

}
