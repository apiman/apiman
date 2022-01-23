package io.apiman.manager.test;

import io.apiman.manager.test.junit.ManagerRestTestPlan;
import io.apiman.manager.test.junit.ManagerRestTester;

import org.junit.runner.RunWith;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@RunWith(ManagerRestTester.class)
@ManagerRestTestPlan("test-plans/discoverability-testPlan.xml")
public class DiscoverabilityTest {

}
