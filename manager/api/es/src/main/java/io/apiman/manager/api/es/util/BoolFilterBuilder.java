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
import java.util.ArrayList;
import java.util.List;

/**
 * @author ewittman
 */
@SuppressWarnings("nls")
public class BoolFilterBuilder extends AbstractQueryBuilder {

    private List<QueryBuilder> filters = new ArrayList<>();

    /**
     * Constructor.
     * @param filters
     */
    public BoolFilterBuilder(QueryBuilder... filters) {
        for (QueryBuilder filter : filters) {
            this.filters.add(filter);
        }
    }

    /**
     * @param filter
     */
    public void add(QueryBuilder filter) {
        this.filters.add(filter);
    }

    /**
     * @see io.apiman.manager.api.es.util.AbstractQueryBuilder#doXContent(io.apiman.manager.api.es.util.XContentBuilder)
     */
    @Override
    protected void doXContent(XContentBuilder builder) throws IOException {
        builder.field("bool");
        for (QueryBuilder query : filters) {
            query.toXContent(builder);
        }
    }
}
