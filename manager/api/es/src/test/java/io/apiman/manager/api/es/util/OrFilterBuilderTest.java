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

import org.junit.Test;

/**
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("nls")
public class OrFilterBuilderTest {

    /**
     * Test method for {@link io.apiman.manager.api.es.util.BoolFilterBuilder#doXContent(io.apiman.manager.api.es.util.XContentBuilder)}.
     */
    @Test
    public void test() throws IOException {
//        String actual = FilterBuilders.orFilter(
//                FilterBuilders.missingFilter("deleted"),
//                FilterBuilders.termFilter("deleted", false)).string();
//        Assert.assertEquals("{\"or\":{\"filters\":[{\"missing\":{\"field\":\"deleted\"}},{\"term\":{\"deleted\":false}}]}}", actual);
    }

}
