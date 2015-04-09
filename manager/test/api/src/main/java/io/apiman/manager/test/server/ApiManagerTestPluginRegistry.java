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
package io.apiman.manager.test.server;

import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.common.plugin.PluginUtils;
import io.apiman.manager.api.core.plugin.AbstractPluginRegistry;

import java.io.File;
import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.io.FileUtils;

/**
 * A concrete implementation of the plugin registry.  This one is used for 
 * testing only.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ApiManagerTestPluginRegistry extends AbstractPluginRegistry {

    /**
     * Creates the temp directory to use for the plugin registry.  This will put
     * the temp dir in the target directory so it gets cleaned when a maven 
     * build is done.
     */
    private static File getTestPluginDir() {
        File targetDir = new File("target").getAbsoluteFile(); //$NON-NLS-1$
        if (!targetDir.isDirectory()) {
            throw new RuntimeException("Maven 'target' directory does not exist!"); //$NON-NLS-1$
        }
        File pluginDir = new File(targetDir, "plugintmp"); //$NON-NLS-1$
        return pluginDir;
    }

    /**
     * Constructor.
     */
    public ApiManagerTestPluginRegistry() {
        super(getTestPluginDir());
    }
    
    /**
     * @see io.apiman.manager.api.core.plugin.AbstractPluginRegistry#downloadPlugin(java.io.File, io.apiman.common.plugin.PluginCoordinates)
     */
    @Override
    protected void downloadPlugin(File pluginFile, PluginCoordinates coordinates) {
        String testM2Path = System.getProperty("apiman.test.m2-path"); //$NON-NLS-1$
        if (testM2Path == null) {
            return;
        }
        File testM2Dir = new File(testM2Path).getAbsoluteFile();
        File pluginArtifactFile = new File(testM2Dir, PluginUtils.getMavenPath(coordinates));
        if (!pluginArtifactFile.isFile()) {
            return;
        }
        try {
            FileUtils.copyFile(pluginArtifactFile, pluginFile);
        } catch (IOException e) {
            pluginArtifactFile.delete();
        }
    }

}
