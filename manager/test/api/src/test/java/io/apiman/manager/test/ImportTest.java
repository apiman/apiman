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

import io.apiman.manager.test.junit.ManagerRestTestGatewayLog;
import io.apiman.manager.test.junit.ManagerRestTestPlan;
import io.apiman.manager.test.junit.ManagerRestTester;
import org.junit.runner.RunWith;

/**
 * Runs the "import" test plan.
 *
 * @author eric.wittmann@redhat.com
 */
@RunWith(ManagerRestTester.class)
@ManagerRestTestGatewayLog(
        "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/apis\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/clients\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/apis\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/apis\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/apis\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/apis\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/apis\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/apis\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/apis\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/apis\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/apis\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/clients\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/clients\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/clients\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/clients\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/clients\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/clients\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/clients\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/clients\n"
          + "GET:/mock-gateway/system/status\n"
          + "PUT:/mock-gateway/clients\n"
  )
public class ImportTest {

  @ManagerRestTestPlan("test-plans/import/import-testPlan.xml")
  public void importTestPlan(){}

  // Order=1 to add a second test case
  @ManagerRestTestPlan(value = "test-plans/import/importDeveloper-testPlan.xml", order = 1)
  public void importDeveloperTestPlan(){}

  @ManagerRestTestPlan(value = "test-plans/import/import-julian-testPlan.xml", order = 2)
  public void importJulianExample(){}

}
