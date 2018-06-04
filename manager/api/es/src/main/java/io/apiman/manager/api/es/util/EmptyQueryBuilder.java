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

/**
 * Empty query
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class EmptyQueryBuilder extends AbstractQueryBuilder {

    private QueryBuilder queryBuilder;

    public EmptyQueryBuilder(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    @Override
    protected void doXContent(XContentBuilder builder) throws IOException {
        builder.field("query"); //$NON-NLS-1$
        queryBuilder.toXContent(builder);
    }

}
