/**
 * 
 */
package io.apiman.osgi.pax.testing;

import io.apiman.osgi.pax.testing.util.ElasticSearchEmbed;
import io.apiman.osgi.pax.testing.util.HttpTestClient;
import io.apiman.osgi.pax.testing.util.KarafBaseTest;
import io.apiman.test.common.plan.TestGroupType;
import io.apiman.test.common.plan.TestPlan;
import io.apiman.test.common.plan.TestType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Test Gateway API
 */
@RunWith(PaxExam.class)
public class GatewayApiTest extends TestPlanUtil {

	private static final Logger LOG = LoggerFactory.getLogger(GatewayApiTest.class);
	private final String Plan_To_Test = "test-plans/api/api-testPlan.xml";
	private static final String EXPECTED_CONTENT = "{\"id\":\"apiman-gateway-api\",\"name\":\"API Gateway REST API\",\"description\":\"The API Gateway REST API is used by the API Manager to publish APIs and register clients.  You can use it directly if you wish, but if you are utilizing the API Manager then it's probably best to avoid invoking this API directly.\",\"version\":\"1.2.2-SNAPSHOT\",\"up\":true}";

	private Bundle warBundle;
	protected HttpTestClient testClient;
	private ElasticSearchEmbed es;

	@Configuration
	public Option[] config() {
		return combine(baseConfig());
	}

	@Test
	public void testCheckSystemStatus() throws Exception {
		testClient.testWebPath("https://localhost:8444/apiman-gateway-api/system/status",EXPECTED_CONTENT,200,true);
	}

	@Before
	public void setUp() throws Exception {

		es = new ElasticSearchEmbed();
		es.launch();

		// Load Test Plan, Test Group and TestType
		loadTestPlans(Plan_To_Test);

		List<TestPlanUtil.TestInfo> restTests = new ArrayList<>();

		for (TestPlanUtil.TestPlanInfo planInfo : testPlans) {
			TestPlan plan = planInfo.plan;
			List<TestGroupType> groups = plan.getTestGroup();
			for(TestGroupType group : groups) {
				List<TestType> testTypeList = group.getTest();
				for(TestType test : testTypeList) {
					TestPlanUtil.TestInfo testInfo = new TestPlanUtil.TestInfo();
					if (testPlans.size() > 1) {
						testInfo.name = planInfo.name + " / " + test.getName();
					} else {
						testInfo.name = test.getName();
					}
					testInfo.plan = planInfo;
					testInfo.group = group;
					testInfo.test = test;
					restTests.add(testInfo);
				}
			}
		}

		for(TestInfo info : restTests) {
			System.out.println("Test Name : " + info.name + " of the Group : " + info.group.getName() + " AND Plan : " + info.plan.name);
		}

		testClient = new HttpTestClient();

		initWebListener();
		final String bundlePath = "mvn:io.apiman/apiman-gateway-osgi-api/1.2.2-SNAPSHOT";
		warBundle = installAndStartBundle(bundlePath);
		waitForWebListener();
	}

	@After
	public void tearDown() throws Exception {
		if (warBundle != null) {
			warBundle.stop();
			warBundle.uninstall();
		}
		testClient.close();
		testClient = null;
	}
}