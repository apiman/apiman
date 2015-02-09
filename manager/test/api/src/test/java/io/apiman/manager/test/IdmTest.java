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
package io.apiman.manager.test;

import io.apiman.manager.test.util.AbstractTestPlanTest;
import io.apiman.manager.test.util.ManagerTestUtils;
import io.apiman.manager.test.util.ManagerTestUtils.TestType;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Runs the "searching" test plan.
 *
 * @author eric.wittmann@redhat.com
 */
public class IdmTest extends AbstractTestPlanTest {
    
    @BeforeClass
    public static void setup() throws Exception {
        ManagerTestUtils.setTestType(TestType.es);
        AbstractTestPlanTest.setup();
    }

    @Test
    public void test() {
        runTestPlan("test-plans/idm-testPlan.xml", IdmTest.class.getClassLoader()); //$NON-NLS-1$
    }

}
