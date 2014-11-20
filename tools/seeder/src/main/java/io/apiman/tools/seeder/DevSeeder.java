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
package io.apiman.tools.seeder;

import io.apiman.test.common.util.TestPlanRunner;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
                try { Thread.sleep(15000); } catch (InterruptedException e) { }
                System.out.println("Seeding the API Management Development Environment"); //$NON-NLS-1$
                doSeeding();
                System.out.println("Done Seeding (Success)"); //$NON-NLS-1$
            }
        }).start();
    }

    /**
     * Actually seed the environment with data.
     */
    protected void doSeeding() {
        try {
            TestPlanRunner runner = new TestPlanRunner("http://localhost:8080/apiman"); //$NON-NLS-1$
            runner.runTestPlan("scripts/seed-dev-environment-testPlan.xml", getClass().getClassLoader()); //$NON-NLS-1$
            
            System.setProperty("apiman-tools-dev-seeder.endpoints.datetime", "http://localhost:8080/services/datetime/"); //$NON-NLS-1$ //$NON-NLS-2$
            runner = new TestPlanRunner("http://localhost:8080/apiman-gateway"); //$NON-NLS-1$
            runner.runTestPlan("scripts/seed-runtime-testPlan.xml", getClass().getClassLoader()); //$NON-NLS-1$
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage()); //$NON-NLS-1$
            System.out.println("Done Seeding (!ERROR!)"); //$NON-NLS-1$
        }
    }

}
