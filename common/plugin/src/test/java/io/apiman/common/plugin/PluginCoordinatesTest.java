/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.common.plugin;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link PluginCoordinates}.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class PluginCoordinatesTest {

    /**
     * Test method for {@link io.apiman.common.plugin.PluginCoordinates#fromPolicySpec(java.lang.String)}.
     */
    @Test
    public void testFromPolicySpec() {
        PluginCoordinates coordinates = PluginCoordinates.fromPolicySpec("plugin:io.apiman.test:testPlugin:1.0.0.Final:plug:war/io.apiman.test.PolicyImpl");
        Assert.assertNotNull(coordinates);
        Assert.assertEquals("io.apiman.test", coordinates.getGroupId());
        Assert.assertEquals("testPlugin", coordinates.getArtifactId());
        Assert.assertEquals("1.0.0.Final", coordinates.getVersion());
        Assert.assertEquals("plug", coordinates.getClassifier());
        Assert.assertEquals("war", coordinates.getType());

        coordinates = PluginCoordinates.fromPolicySpec("plugin:io.apiman.test:testPlugin:1.0.0.Final:war/io.apiman.test.PolicyImpl");
        Assert.assertNotNull(coordinates);
        Assert.assertEquals("io.apiman.test", coordinates.getGroupId());
        Assert.assertEquals("testPlugin", coordinates.getArtifactId());
        Assert.assertEquals("1.0.0.Final", coordinates.getVersion());
        Assert.assertNull(coordinates.getClassifier());
        Assert.assertEquals("war", coordinates.getType());

        coordinates = PluginCoordinates.fromPolicySpec("plugin:io.apiman.test:testPlugin:1.0.0.Final/io.apiman.test.PolicyImpl");
        Assert.assertNotNull(coordinates);
        Assert.assertEquals("io.apiman.test", coordinates.getGroupId());
        Assert.assertEquals("testPlugin", coordinates.getArtifactId());
        Assert.assertEquals("1.0.0.Final", coordinates.getVersion());
        Assert.assertNull(coordinates.getClassifier());
        Assert.assertEquals("war", coordinates.getType());
    }

}
