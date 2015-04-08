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
package io.apiman.manager.api.core.plugin;

import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginCoordinates;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link AbstractPluginRegistry}.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class AbstractPluginRegistryTest {

    /**
     * Test method for {@link io.apiman.manager.api.core.plugin.AbstractPluginRegistry#loadPlugin(io.apiman.common.plugin.PluginCoordinates)}.
     * @throws Exception any exception
     */
    @Test
    public void testLoadPlugin() throws Exception {
        File targetDir = new File("target").getAbsoluteFile();
        File tmpDir = new File(targetDir, "_plugintmp");
        AbstractPluginRegistry registry = new TestPluginRegistry(tmpDir);
        PluginCoordinates coords = new PluginCoordinates("io.apiman.test", "apiman-test-plugin", "1.0");
        Plugin plugin = registry.loadPlugin(coords);
        Assert.assertNotNull(plugin);;
        Assert.assertEquals("Test Plugin", plugin.getName());
        Assert.assertEquals("The first ever plugin for testing.", plugin.getDescription());
    }

    public static class TestPluginRegistry extends AbstractPluginRegistry {

        /**
         * Constructor.
         * @param pluginsDir the plugin's directory
         */
        public TestPluginRegistry(File pluginsDir) {
            super(pluginsDir);
        }
        
        /**
         * @see io.apiman.manager.api.core.plugin.AbstractPluginRegistry#downloadFromMavenRepo(java.io.File, io.apiman.common.plugin.PluginCoordinates, java.net.URL)
         */
        @Override
        protected boolean downloadFromMavenRepo(File pluginFile, PluginCoordinates coordinates,
                URL mavenRepoUrl) {
            // Testing - don't actually download from maven central here!
            File file = new File("src/test/resources/plugin.war");
            if (!file.isFile()) {
                return false;
            }
            try {
                FileUtils.copyFile(file, pluginFile);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        
    }
    
}
