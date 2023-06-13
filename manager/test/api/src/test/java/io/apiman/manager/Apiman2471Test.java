/*
 * Copyright 2023 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */
package io.apiman.manager;

import io.apiman.manager.test.junit.ManagerRestTestPlan;
import io.apiman.manager.test.junit.ManagerRestTester;
import org.junit.runner.RunWith;

/**
 * Runs the test plan that reproduces https://github.com/apiman/apiman/issues/2471
 */
@RunWith(ManagerRestTester.class)
@ManagerRestTestPlan("test-plans/apiman-2471-delete-testPlan.xml")
public class Apiman2471Test {
}
