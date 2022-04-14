package io.apiman.manager.test;

import io.apiman.manager.test.junit.ManagerRestTestPlan;
import io.apiman.manager.test.junit.ManagerRestTester;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@RunWith(ManagerRestTester.class)
@ManagerRestTestPlan("test-plans/approvals-testPlan.xml")
public class ApprovalsTest {

    @BeforeClass
    public static void before() {
        System.setProperty("apiman-manager.idm.discoverability.apiuser.source", "IDM_ROLE");
        System.setProperty("apiman-manager.idm.discoverability.apiuser.discoverabilities", "PORTAL, ANONYMOUS, FULL_PLATFORM_MEMBERS");
        System.setProperty("apiman-manager.idm.discoverability.devportaluser.source", "IDM_ROLE");
        System.setProperty("apiman-manager.idm.discoverability.devportaluser.discoverabilities","PORTAL, ANONYMOUS");
    }
}
