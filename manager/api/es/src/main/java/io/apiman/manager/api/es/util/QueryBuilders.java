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
 * Mimics the ES query builder syntax.  Builds ES queries, but without using the
 * enormous elasticsearch.jar dependency.
 */
public final class QueryBuilders {
    
    /**
     * Constructor.
     */
    private QueryBuilders() {
    }

    public static MatchAllQueryBuilder matchAllQuery() {
        return new MatchAllQueryBuilder();
    }

    public static FilteredQueryBuilder filteredQuery(QueryBuilder queryBuilder, QueryBuilder filterBuilder) {
        return new FilteredQueryBuilder(queryBuilder, filterBuilder);
    }
    
    public static TermsQueryBuilder termsQuery(String name, String... values) {
        return new TermsQueryBuilder(name, values);
    }

    public static WildcardQueryBuilder wildcardQuery(String name, String query) {
        return new WildcardQueryBuilder(name, query);
    }

}
