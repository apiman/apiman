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
package io.apiman.manager.test.util;

/**
 * Some test util methods.
 *
 * @author eric.wittmann@redhat.com
 */
public class ManagerTestUtils {
    
    public static enum TestType {
        jpa, es;
    }

    public static final String TEST_TYPE = "apiman-test.type"; //$NON-NLS-1$

    /**
     * @param type the test type
     */
    public static final void setTestType(TestType type) {
        System.setProperty(TEST_TYPE, type.name());
    }
    
    /**
     * @return what 'type' of test to run
     */
    public static final TestType getTestType() {
        return TestType.valueOf(System.getProperty(TEST_TYPE, TestType.jpa.name()));
    }
}
