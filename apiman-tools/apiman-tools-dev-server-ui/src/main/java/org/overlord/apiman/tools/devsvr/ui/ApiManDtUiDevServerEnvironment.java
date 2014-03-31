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
package org.overlord.apiman.tools.devsvr.ui;

import java.io.File;
import java.io.FileWriter;
import java.util.Properties;

import org.overlord.commons.dev.server.DevServerEnvironment;

/**
 * Holds information about the apiman development runtime environment.
 * @author eric.wittmann@redhat.com
 */
public class ApiManDtUiDevServerEnvironment extends DevServerEnvironment {

    /**
     * Constructor.
     * @param args
     */
    public ApiManDtUiDevServerEnvironment(String[] args) {
        super(args);
    }
    
    /**
     * @see org.overlord.commons.dev.server.DevServerEnvironment#createAppConfigs()
     */
    @Override
    public void createAppConfigs() throws Exception {
        super.createAppConfigs();
        
        File dir = new File(getTargetDir(), "overlord-apps"); //$NON-NLS-1$
        dir.mkdirs();

        File configFile1 = new File(dir, "apiman-dt-ui-overlordapp.properties"); //$NON-NLS-1$
        Properties props = new Properties();
        props.setProperty("overlordapp.app-id", "apiman-dt-ui"); //$NON-NLS-1$ //$NON-NLS-2$
        props.setProperty("overlordapp.href", "/apiman/index.html"); //$NON-NLS-1$ //$NON-NLS-2$
        props.setProperty("overlordapp.label", "API Management"); //$NON-NLS-1$ //$NON-NLS-2$
        props.setProperty("overlordapp.primary-brand", "JBoss Overlord"); //$NON-NLS-1$ //$NON-NLS-2$
        props.setProperty("overlordapp.secondary-brand", "API Management"); //$NON-NLS-1$ //$NON-NLS-2$
        props.store(new FileWriter(configFile1), "APIMan UI application"); //$NON-NLS-1$
    }

}
