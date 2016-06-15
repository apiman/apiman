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

package io.apiman.manager.api.hawkular;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author eric.wittmann@gmail.com
 */
public class TopNSortedMapTest {

    /**
     * Test method for {@link io.apiman.manager.api.hawkular.TopNSortedMap#put(java.lang.Comparable, java.lang.Comparable)}.
     */
    @SuppressWarnings("nls")
    @Test
    public void testPut() {
        TopNSortedMap<String, Long> top5 = new TopNSortedMap<>(5);
        top5.put("10", 10l);
        Assert.assertEquals(1, top5.size());
        Assert.assertEquals((Long) 10L, (Long) top5.get("10")); //$NON-NLS-1$

        top5.put("5", 5l);
        top5.put("20", 20l);
        top5.put("15", 15l);
        Assert.assertEquals(4, top5.size());
        Assert.assertEquals((Long) 10L, (Long) top5.get("10")); //$NON-NLS-1$
        Assert.assertEquals((Long) 20L, (Long) top5.get("20")); //$NON-NLS-1$

        top5.put("3", 3l);
        top5.put("50", 50l);
        top5.put("65", 65l);
        top5.put("75", 75l);
        top5.put("55", 55l);
        Assert.assertEquals(5, top5.size());
        Assert.assertEquals((Long) null, (Long) top5.get("10")); //$NON-NLS-1$
        Assert.assertEquals((Long) 20L, (Long) top5.get("20")); //$NON-NLS-1$
        Assert.assertEquals((Long) null, (Long) top5.get("5")); //$NON-NLS-1$
        Assert.assertEquals((Long) 55L, (Long) top5.get("55")); //$NON-NLS-1$
        Assert.assertEquals((Long) 75L, (Long) top5.get("75")); //$NON-NLS-1$

    }

}
