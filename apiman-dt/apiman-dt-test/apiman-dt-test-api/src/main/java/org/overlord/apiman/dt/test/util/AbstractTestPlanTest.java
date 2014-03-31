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
package org.overlord.apiman.dt.test.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.overlord.apiman.dt.test.server.DtApiTestServer;
import org.overlord.apiman.test.common.util.TestPlanRunner;

/**
 * Base class for all junit integration tests for dt api.
 * 
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractTestPlanTest {

    private static DtApiTestServer testServer = new DtApiTestServer();

    @BeforeClass
    public static void setup() throws Exception {
        testServer.start();
    }

    /**
     * Runs the given test plan.
     * @param planPath
     * @param classLoader
     */
    protected void runTestPlan(String planPath, ClassLoader classLoader) {
        String baseApiUrl = "http://localhost:" + testServer.serverPort() + "/apiman-dt-api"; //$NON-NLS-1$ //$NON-NLS-2$
        TestPlanRunner runner = new TestPlanRunner(baseApiUrl);
        runner.runTestPlan(planPath, classLoader);
    }

    @AfterClass
    public static void shutdown() throws Exception {
        testServer.stop();
    }

}
