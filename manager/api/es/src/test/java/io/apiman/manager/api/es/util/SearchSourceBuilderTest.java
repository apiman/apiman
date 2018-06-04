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
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("nls")
public class SearchSourceBuilderTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     */
    @Test
    public void test1() throws IOException {
        QueryBuilder qb =
            FilterBuilders.boolFilter(
                FilterBuilders.filter(
                    FilterBuilders.termFilter("groupId", "GROUP"),
                    FilterBuilders.termFilter("artifactId", "ARTY"))
            );
        SearchSourceBuilder builder = new SearchSourceBuilder().query(qb).size(2);
        String actual = builder.string();
        Assert.assertEquals(
                "{\"size\":2,\"query\":{\"bool\":{\"filter\":[{\"term\":{\"groupId\":\"GROUP\"}},{\"term\":{\"artifactId\":\"ARTY\"}}]}}}",
                actual);
        mapper.readTree(actual);
    }

    /**
     */
    @Test
    public void test2() throws IOException {
        String[] fields = {"id", "artifactId", "groupId", "version", "classifier", "type", "name",
                "description", "createdBy", "createdOn"};
        QueryBuilder query = FilterBuilders.notExistOrFalse("deleted");
        SearchSourceBuilder builder = new SearchSourceBuilder().fetchSource(fields, null).query(query)
                .sort("name.raw", SortOrder.ASC).size(200); //$NON-NLS-1$
        String actual = builder.string();
        Assert.assertEquals(
                "{\"size\":200,\"query\":{\"bool\":{\"should\":[{\"bool\":{\"must_not\":[{\"term\":{\"deleted\":true}}]}},{\"bool\":{\"must_not\":[{\"exists\":{\"field\":\"deleted\"}}]}}]}},\"sort\":[{\"name.raw\":{\"order\":\"asc\"}}],\"_source\":{\"include\":[\"id\",\"artifactId\",\"groupId\",\"version\",\"classifier\",\"type\",\"name\",\"description\",\"createdBy\",\"createdOn\"]}}",
                actual);
        mapper.readTree(actual);
    }

    /**
     */
    @Test
    public void test3() throws IOException {
        String[] fields = {"id", "name", "description","type"};
        SearchSourceBuilder builder = new SearchSourceBuilder().fetchSource(fields, null).sort("name.raw", SortOrder.ASC).size(100); //$NON-NLS-1$
        String actual = builder.string();
        Assert.assertEquals(
                "{\"size\":100,\"sort\":[{\"name.raw\":{\"order\":\"asc\"}}],\"_source\":{\"include\":[\"id\",\"name\",\"description\",\"type\"]}}",
                actual);
        mapper.readTree(actual);
    }

    /**
     */
    @Test
    public void test4() throws IOException {
        Set<String> organizationIds = new LinkedHashSet<>();
        organizationIds.add("ORG1");
        organizationIds.add("ORG2");
        organizationIds.add("ORG3");
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .sort("organizationName.raw", SortOrder.ASC)
                .sort("name.raw", SortOrder.ASC)
                .size(500);
        TermsQueryBuilder query = QueryBuilders.termsQuery("organizationId", organizationIds.toArray(new String[organizationIds.size()])); //$NON-NLS-1$
        builder.query(query);
        String actual = builder.string();
        Assert.assertEquals(
                "{\"size\":500,\"query\":{\"terms\":{\"organizationId\":[\"ORG1\",\"ORG2\",\"ORG3\"]}},\"sort\":[{\"organizationName.raw\":{\"order\":\"asc\"}},{\"name.raw\":{\"order\":\"asc\"}}]}",
                actual);
        mapper.readTree(actual);
    }

    /**
     */
    @Test
    public void test5() throws IOException {
        QueryBuilder query =
            FilterBuilders.boolFilter(
                    FilterBuilders.filter(
                            FilterBuilders.termFilter("organizationId", "ORG"),
                            FilterBuilders.termFilter("clientId", "CLIENT"))
                    );

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .sort("createdOn", SortOrder.DESC)
                .query(query)
                .size(500);
        String actual = builder.string();
        System.out.println(actual);
        Assert.assertEquals(
                "{\"size\":500,\"query\":{\"bool\":{\"filter\":[{\"term\":{\"organizationId\":\"ORG\"}},{\"term\":{\"clientId\":\"CLIENT\"}}]}},\"sort\":[{\"createdOn\":{\"order\":\"desc\"}}]}",
                actual);
        mapper.readTree(actual);
    }

}
