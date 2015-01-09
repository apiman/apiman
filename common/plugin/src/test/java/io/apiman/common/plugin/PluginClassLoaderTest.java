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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for the plugin class loader.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class PluginClassLoaderTest {

    /**
     * Test method for {@link io.apiman.common.plugin.PluginClassLoader#loadClass(java.lang.String)}.
     * @throws Exception 
     */
    @Test
    public void testLoadClass() throws Exception {
        File file = new File("src/test/resources/plugin.war");
        if (!file.exists()) {
            throw new Exception("Failed to find test WAR: plugin.war at: " + file.getAbsolutePath());
        }
        PluginClassLoader classloader = new TestPluginClassLoader(file);
        Class<?> class1 = classloader.loadClass("io.apiman.quickstarts.plugin.PluginMain");
        Method mainMethod = class1.getMethod("main", new String[0].getClass());
        mainMethod.invoke(null, new Object[1]);
        
        try {
            classloader.loadClass("io.apiman.notfound.MyClass");
            Assert.fail("Should have gotten a classnotfound here!");
        } catch (Exception e) {
            Assert.assertEquals("io.apiman.notfound.MyClass", e.getMessage());
        }
    }

    /**
     * Test method for {@link io.apiman.common.plugin.PluginClassLoader#loadClass(java.lang.String)}.
     * @throws Exception 
     */
    @Test
    public void testLoadClassWithDeps() throws Exception {
        File file = new File("src/test/resources/plugin-with-deps.war");
        if (!file.exists()) {
            throw new Exception("Failed to find test WAR: plugin-with-deps.war at: " + file.getAbsolutePath());
        }
        PluginClassLoader classloader = new TestPluginClassLoader(file);
        Class<?> class1 = classloader.loadClass("io.apiman.quickstarts.plugin.deps.DepsPluginMain");
        Method mainMethod = class1.getMethod("main", new String[0].getClass());
        mainMethod.invoke(null, new Object[1]);
    }

    /**
     * Test method for {@link io.apiman.common.plugin.PluginClassLoader#getResource(String)}.
     * @throws Exception 
     */
    @Test
    public void testGetResource() throws Exception {
        File file = new File("src/test/resources/plugin.war");
        if (!file.exists()) {
            throw new Exception("Failed to find test WAR: plugin.war at: " + file.getAbsolutePath());
        }
        PluginClassLoader classloader = new TestPluginClassLoader(file);
        URL resource = classloader.getResource("META-INF/maven/io.apiman/apiman-quickstarts-plugin/pom.properties");
        Assert.assertNotNull(resource);
        resource = classloader.getResource("META-INF/maven/io.apiman/apiman-quickstarts-plugin/not.found");
        Assert.assertNull(resource);
    }

    /**
     * Test method for {@link io.apiman.common.plugin.PluginClassLoader#getPolicyDefinitionResources()}.
     * @throws Exception 
     */
    @Test
    public void testGetPolicyDefinitionResources() throws Exception {
        File file = new File("src/test/resources/plugin-with-policyDefs.war");
        if (!file.exists()) {
            throw new Exception("Failed to find test WAR: plugin-with-policyDefs.war at: " + file.getAbsolutePath());
        }
        PluginClassLoader classloader = new TestPluginClassLoader(file);
        List<URL> resources = classloader.getPolicyDefinitionResources();
        Assert.assertNotNull(resources);
        Assert.assertEquals(2, resources.size());
        URL url = resources.get(0);
        Assert.assertNotNull(url);
        Assert.assertTrue(url.toString().contains("META-INF/apiman/policyDefs"));
    }
    
    public static class TestPluginClassLoader extends PluginClassLoader {

        /**
         * Constructor.
         * @param pluginArtifactFile
         * @throws IOException
         */
        public TestPluginClassLoader(File pluginArtifactFile) throws IOException {
            super(pluginArtifactFile);
        }
        
        /**
         * @see io.apiman.common.plugin.PluginClassLoader#createWorkDir(java.io.File)
         */
        @Override
        protected File createWorkDir(File pluginArtifactFile) throws IOException {
            File dir = new File(new File("target/_plugintmp").getAbsoluteFile(), pluginArtifactFile.getName());
            dir.mkdirs();
            return dir;
        }
        
    }
    
}
