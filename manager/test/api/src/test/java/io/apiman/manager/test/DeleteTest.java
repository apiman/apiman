package io.apiman.manager.test;

import io.apiman.manager.test.junit.ManagerRestTestPlan;
import io.apiman.manager.test.junit.ManagerRestTester;

import org.junit.runner.RunWith;

/**
 * Runs the "delete" test plan.
 *
 * @author eric.wittmann@redhat.com
 */
@RunWith(ManagerRestTester.class)
@ManagerRestTestPlan("test-plans/delete-testPlan.xml")
public class DeleteTest {
}
