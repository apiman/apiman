/**
 * 
 */
package io.apiman.osgi.pax.testing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Test service System/status of the Gateway
 */
@RunWith(PaxExam.class)
public class GatewayApiSystemStatusTest extends KarafBaseTest {

	Logger LOG = LoggerFactory.getLogger(GatewayApiSystemStatusTest.class);

	private Bundle warBundle;

	protected HttpTestClient testClient;

	@Configuration
	public Option[] config() {
		return combine(baseConfig());
	}

	@Test
	public void testSystemStatus() throws Exception {
		testClient.testWebPath("https://localhost:8444/apiman-gateway-api/system/status","",200,true);
	}

	@Before
	public void setUp() throws Exception {
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