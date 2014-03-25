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
package org.overlord.apiman.tools.dev.seeder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.overlord.apiman.test.common.util.TestPlanRunner;

/**
 * Seeds the dev environment.
 *
 * @author eric.wittmann@redhat.com
 */
public class DevSeeder implements ServletContextListener {
    
    /**
     * Constructor.
     */
    public DevSeeder() {
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try { Thread.sleep(5000); } catch (InterruptedException e) { }
                System.out.println("Seeding the API Management Development Environment");
                doSeeding();
                System.out.println("Done Seeding (Success)");
            }
        }).start();;
    }

    /**
     * Actually seed the environment with data.
     */
    protected void doSeeding() {
        try {
            TestPlanRunner runner = new TestPlanRunner("http://localhost:8080/apiman-dt-api");
            runner.runTestPlan("scripts/seed-dev-environment-testPlan.xml", getClass().getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Done Seeding (!ERROR!)");
        }
    }

}
