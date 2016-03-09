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

import java.io.IOException;

/**
 * @author ewittman
 */
public class FilteredQueryBuilder extends AbstractQueryBuilder {
    
    private QueryBuilder queryBuilder;
    private QueryBuilder filterBuilder;

    /**
     * Constructor.
     * @param queryBuilder
     * @param filterBuilder
     */
    public FilteredQueryBuilder(QueryBuilder queryBuilder, QueryBuilder filterBuilder) {
        this.queryBuilder = queryBuilder;
        this.filterBuilder = filterBuilder;
    }
    
    /**
     * @see io.apiman.manager.api.es.util.AbstractQueryBuilder#doXContent(io.apiman.manager.api.es.util.XContentBuilder)
     */
    @Override
    @SuppressWarnings("nls")
    protected void doXContent(XContentBuilder builder) throws IOException {
        builder.startObject("filtered");
        if (queryBuilder != null) {
            builder.field("query");
            queryBuilder.toXContent(builder);
        }
        if (filterBuilder != null) {
            builder.field("filter");
            filterBuilder.toXContent(builder);
        }
        builder.endObject();
    }

}
