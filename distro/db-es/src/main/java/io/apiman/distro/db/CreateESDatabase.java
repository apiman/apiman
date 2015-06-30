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
package io.apiman.distro.db;

import io.apiman.manager.test.util.ManagerTestUtils;
import io.apiman.manager.test.util.ManagerTestUtils.TestType;
import io.apiman.test.common.util.TestUtil;

import java.io.File;

/**
 * Unit test that creates an H2 database file by firing up the API Manager and sending a
 * bunch of REST requests to configure it.  When this test is complete there should be a
 * valid H2 database located in target/classes (and thus be included in the JAR).
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class CreateESDatabase extends CreateH2Database {

    /**
     * The test suite main entry point.
     * @param args
     * @throws Exception
     */
    public static void main(String [] args) throws Exception {
        CreateESDatabase ch2d = new CreateESDatabase();
        ch2d.setup();
        ch2d.startServer();
        try {
            ch2d.create();
        } finally {
            ch2d.stopServer();
        }
    }

    /**
     * @see io.apiman.distro.db.CreateH2Database#setup()
     */
    @Override
    public void setup() throws Exception {
        File targetClassesDir = new File("target/classes").getAbsoluteFile();
        if (!targetClassesDir.exists()) {
            targetClassesDir.mkdirs();
        }

        System.out.println("------------------------------------------------");
        System.out.println("Setting elasticsearch output path: " + targetClassesDir.toString());
        System.out.println("------------------------------------------------");

        TestUtil.setProperty("apiman.test.es-home", targetClassesDir.toString());
        TestUtil.setProperty("apiman.test.admin-user-only", "true");
        TestUtil.setProperty("apiman.test.es-cluster-name", "apiman");
        TestUtil.setProperty("apiman.test.es-persistence", "true");
        ManagerTestUtils.setTestType(TestType.es);
    }

    /**
     * @see io.apiman.distro.db.CreateH2Database#stopServer()
     */
    @Override
    protected void stopServer() {
        try {
            Thread.sleep(2000); // allow ES time to refresh/index
            TestUtil.setProperty("apiman.test.es-delete-index", "false");
            super.stopServer();
            testServer.getESNode().close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
