/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.manager.api.beans;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link BeanUtils}.
 *
 * @author eric.wittmann@redhat.com
 */
public class BeanUtilsTest {

    /**
     * Test method for {@link io.apiman.manager.api.beans.BeanUtils#idFromName(java.lang.String)}.
     */
    @SuppressWarnings("nls")
    @Test
    public void testIdFromName() {
        assertIdFromName("EricWittmann", "Eric Wittmann");
        assertIdFromName("DeloitteTouche", "Deloitte & Touche");
        assertIdFromName("JBoss_Overlord", "JBoss_Overlord");
        assertIdFromName("Red--Hat", "!Red--Hat?");
        assertIdFromName("Org.With.Periods", "Org.With.Periods");
        assertIdFromName("my-project", "my-project");
        assertIdFromName("MyInjectionimgsrca.fsdn.comsdtopicsspace_64.pngaltSpacetitleSpaceheight64width64", 
                "My Injection: <img src=\\\"//a.fsdn.com/sd/topics/space_64.png\\\" alt=\\\"Space\\\" title=\\\"Space\\\" height=\\\"64\\\" width=\\\"64\\\">");
        assertIdFromName("1.0.7-SNAPSHOT", "1.0.7-SNAPSHOT");
        assertIdFromName("2.1.0_Final", "2.1.0_Final");
    }

    /**
     * Creates an ID from a name and asserts that the result is as expected.
     * @param expected
     * @param name
     */
    private void assertIdFromName(String expected, String name) {
        String id = BeanUtils.idFromName(name);
        Assert.assertEquals(expected, id);
    }

}
