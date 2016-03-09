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
public class SearchSourceBuilder extends AbstractQueryBuilder {
    
    private QueryBuilder query;
    private Integer from;
    private Integer size;
    
    private List<SortInfo> sorts = new ArrayList<>();
    
    private String[] fetchIncludes;
    @SuppressWarnings("unused")
    private String[] fetchExcludes;
    private boolean fetchSource;
    
    /**
     * Constructor.
     */
    public SearchSourceBuilder() {
    }

    public SearchSourceBuilder query(QueryBuilder query) {
        this.query = query;
        return this;
    }

    public SearchSourceBuilder size(int size) {
        this.size = size;
        return this;
    }

    public SearchSourceBuilder fetchSource(String[] includes, String[] excludes) {
        this.fetchIncludes = includes;
        this.fetchExcludes = excludes;
        return this;
    }

    public SearchSourceBuilder sort(String fieldName, SortOrder order) {
        sorts.add(new SortInfo(fieldName, order));
        return this;
    }

    public SearchSourceBuilder from(int from) {
        this.from = from;
        return this;
    }

    public SearchSourceBuilder fetchSource(boolean fetch) {
        this.fetchSource = fetch;
        return this;
    }
    
    /**
     * @see io.apiman.manager.api.es.util.AbstractQueryBuilder#doXContent(io.apiman.manager.api.es.util.XContentBuilder)
     */
    @SuppressWarnings("nls")
    @Override
    protected void doXContent(XContentBuilder builder) throws IOException {
        if (from != null) {
            builder.field("from", from);
        }
        if (size != null) {
            builder.field("size", size);
        }

        if (query != null) {
            builder.field("query");
            query.toXContent(builder);
        }

        if (!sorts.isEmpty()) {
            builder.startArray("sort");
            for (SortInfo sortInfo : sorts) {
                builder.startObject();
                builder.startObject(sortInfo.sortFieldName);
                builder.field("order", sortInfo.sortOrder.toString());
                builder.endObject();
                builder.endObject();
            }
            builder.endArray();
        }

        if (fetchSource) {
            builder.field("_source", true);
        }
        
        if (fetchIncludes != null) {
            builder.field("_source");
            builder.startObject();
            builder.array("include", fetchIncludes);
            builder.endObject();
        }
    }
    
    private static class SortInfo {
        
        public String sortFieldName;
        public SortOrder sortOrder;
        
        /**
         * Constructor.
         * @param fieldName
         * @param order
         */
        public SortInfo(String fieldName, SortOrder order) {
            this.sortFieldName = fieldName;
            this.sortOrder = order;
        }
    }

}
