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

    public static TermFilterBuilder termFilter(String term, String value) {
        return new TermFilterBuilder(term, value);
    }

    public static TermFilterBuilder termFilter(String term, boolean value) {
        return new TermFilterBuilder(term, value);
    }

    public static TermFilterBuilder termFilter(String term, Long value) {
        return new TermFilterBuilder(term, value);
    }

    public static AndFilterBuilder andFilter(QueryBuilder ... filters) {
        return new AndFilterBuilder(filters);
    }

    public static MissingFilterBuilder missingFilter(String name) {
        return new MissingFilterBuilder(name);
    }

    public static OrFilterBuilder orFilter(QueryBuilder ... filters) {
        return new OrFilterBuilder(filters);
    }

}
