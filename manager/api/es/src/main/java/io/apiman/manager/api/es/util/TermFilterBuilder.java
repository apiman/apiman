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
 *
 */
public class TermFilterBuilder extends AbstractQueryBuilder {
    
    private String term;
    private String sValue;
    private Boolean bValue;
    private Long lValue;

    /**
     * Constructor.
     * @param term
     * @param value
     */
    public TermFilterBuilder(String term, String value) {
        this.term = term;
        this.sValue = value;
    }

    /**
     * Constructor.
     * @param term
     * @param value
     */
    public TermFilterBuilder(String term, boolean value) {
        this.term = term;
        this.bValue = value;
    }

    /**
     * Constructor.
     * @param term
     * @param value
     */
    public TermFilterBuilder(String term, Long value) {
        this.term = term;
        this.lValue = value;
    }
    
    /**
     * @see io.apiman.manager.api.es.util.AbstractQueryBuilder#doXContent(io.apiman.manager.api.es.util.XContentBuilder)
     */
    @SuppressWarnings("nls")
    @Override
    protected void doXContent(XContentBuilder builder) throws IOException {
        builder.startObject("term");
        if (sValue != null) {
            builder.field(term, sValue);
        } else if (bValue != null) {
            builder.field(term, bValue);
        } else if (lValue != null) {
            builder.field(term, lValue);
        }
        builder.endObject();
    }

}
