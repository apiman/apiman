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

package io.apiman.tools.devsvr.manager.api;

import io.apiman.manager.test.server.ISeeder;
import io.apiman.manager.test.server.ManagerApiTestServer;
import io.apiman.test.common.util.TestUtil;

import java.io.File;

/**
 * A dev server for APIMan.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("javadoc")
public class ManagerApiDevServer {

    /**
     * Main entry point.
     * @param args
     */
    @SuppressWarnings("nls")
    public static void main(String [] args) throws Exception {
        TestUtil.setProperty(ISeeder.SYSTEM_PROPERTY, ManagerApiDataSeeder.class.getName());
        File m2TestDir = new File(new File("").getAbsoluteFile().getParentFile().getParentFile().getParentFile(),
                "manager/test/api/src/test/resources/test-plan-data/plugins/m2").getAbsoluteFile();
        if (!m2TestDir.isDirectory()) {
            throw new Exception("Failed to find test m2 directory at: " + m2TestDir.getAbsolutePath());
        }
        TestUtil.setProperty("apiman.test.m2-path", m2TestDir.getAbsolutePath());
        ManagerApiTestServer server = new ManagerApiTestServer();
        server.start();
    }

}
