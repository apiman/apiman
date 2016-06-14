package io.apiman.osgi.pax.testing;

import io.apiman.osgi.pax.testing.util.KarafBaseTest;
import io.apiman.test.common.plan.TestGroupType;
import io.apiman.test.common.plan.TestPlan;
import io.apiman.test.common.plan.TestType;
import io.apiman.test.common.util.TestUtil;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestPlanUtil extends KarafBaseTest {

    protected List<TestPlanInfo> testPlans = new ArrayList<>();

    protected void loadTestPlans(String path) throws InitializationError {
        try {
            TestPlanInfo planInfo = new TestPlanInfo();
            planInfo.planPath = path;
            planInfo.name = new File(planInfo.planPath).getName();
            planInfo.plan = TestUtil.loadTestPlan(planInfo.planPath, getClass().getClassLoader());
            testPlans.add(planInfo);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new InitializationError(e);
        }

        if (testPlans.isEmpty()) {
            throw new InitializationError("No TestPlans found ");
        }
    }

    /**
     * Return a list of the TestInfo
     */
    protected List<TestInfo> getTestInfoList() {
        List<TestInfo> children = new ArrayList<>();

        for (TestPlanInfo planInfo : testPlans) {

            List<TestGroupType> groups = planInfo.plan.getTestGroup();
            for (TestGroupType group : groups) {
                for (TestType test : group.getTest()) {
                    TestInfo testInfo = new TestInfo();
                    if (testPlans.size() > 1) {
                        testInfo.name = planInfo.name + " / " + test.getName();
                    } else {
                        testInfo.name = test.getName();
                    }
                    testInfo.plan = planInfo;
                    testInfo.group = group;
                    testInfo.test = test;
                    children.add(testInfo);
                }
            }
        }

        return children;
    }


    public static class TestPlanInfo {
        TestPlan plan;
        String name;
        String planPath;
    }

    public static class TestInfo {
        TestGroupType group;
        TestType test;
        String name;
        TestPlanInfo plan;
    }

}
