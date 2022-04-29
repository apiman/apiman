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
public class DiscoverabilityTest {
    @ManagerRestTestPlan(value = "test-plans/discoverability-simple-testPlan.xml", order = 1)
    public void simple() {}

    @ManagerRestTestPlan(value = "test-plans/discoverability-complex-testPlan.xml", order = 2)
    public void complex() {}
}
