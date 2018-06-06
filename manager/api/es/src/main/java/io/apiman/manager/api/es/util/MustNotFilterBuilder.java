/*
 * Copyright 2018 JBoss Inc
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
 * @author Marc Savy
 */
@SuppressWarnings("nls")
public class MustNotFilterBuilder extends AbstractQueryBuilder {

    private List<QueryBuilder> filters = new ArrayList<>();

    /**
     * Constructor.
     * @param operationType
     * @param filters
     */
    public MustNotFilterBuilder(QueryBuilder... filters) {
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
        builder.startArray("must_not");
        for (QueryBuilder query : filters) {
            query.toXContent(builder);
        }
        builder.endArray();
    }

}
