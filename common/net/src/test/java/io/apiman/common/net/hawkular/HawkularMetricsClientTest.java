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

package io.apiman.common.net.hawkular;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author eric.wittmann@gmail.com
 */
public class HawkularMetricsClientTest {

    /**
     * Test method for {@link io.apiman.common.net.hawkular.HawkularMetricsClient#main(java.lang.String[])}.
     */
    @SuppressWarnings("nls")
    @Test
    public void testEncodeTags() {
        String actual = HawkularMetricsClient.encodeTags(null);
        Assert.assertEquals(null, actual);
        
        Map<String, String> tags = new LinkedHashMap<>();
        actual = HawkularMetricsClient.encodeTags(tags);
        Assert.assertEquals("", actual);

        tags.put("foo", "bar");
        actual = HawkularMetricsClient.encodeTags(tags);
        Assert.assertEquals("foo:bar", actual);

        tags.put("hello", "world");
        actual = HawkularMetricsClient.encodeTags(tags);
        Assert.assertEquals("foo:bar,hello:world", actual);

        tags.put("prop1", "hello world");
        actual = HawkularMetricsClient.encodeTags(tags);
        Assert.assertEquals("foo:bar,hello:world,prop1:hello+world", actual);
    }

}
