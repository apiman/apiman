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

package io.apiman.manager.api.migrator;

import io.apiman.manager.api.migrator.VersionMigrators.Entry;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("nls")
public class VersionMigratorsTest {

    @Test
    public void testVersionComponents1() {
        Entry entry = new Entry("1.2.2.Final", null);
        Assert.assertTrue(entry.isBetween("1.2.0-SNAPSHOT", "1.2.3.Final"));
        Assert.assertTrue(entry.isBetween("0.1.1.CR1", "2.2.7-SNAPSHOT"));

        Assert.assertFalse(entry.isBetween("2.1.3.Final", "2.4.7.Final"));
        Assert.assertFalse(entry.isBetween("1.0.3.Final", "1.0.9.Final"));
        Assert.assertFalse(entry.isBetween("1.0.0.Final", "1.2.2.Final"));
    }

    @Test
    public void testVersionComponents2() {
        Entry entry = new Entry("10.2.783.Final", null);
        Assert.assertTrue(entry.isBetween("1.0.0.Final", "11.7.3-SNAPSHOT"));

        Assert.assertFalse(entry.isBetween("1.2.0-SNAPSHOT", "1.2.3.Final"));
        Assert.assertFalse(entry.isBetween("0.1.1.CR1", "2.2.7-SNAPSHOT"));
        Assert.assertFalse(entry.isBetween("2.1.3.Final", "2.4.7.Final"));
        Assert.assertFalse(entry.isBetween("1.0.3.Final", "1.0.9.Final"));
    }

}
