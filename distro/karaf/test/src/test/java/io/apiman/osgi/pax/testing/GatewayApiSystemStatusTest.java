/**
 * 
 */
package io.apiman.osgi.pax.testing;

import io.apiman.osgi.pax.testing.util.ElasticSearchEmbed;
import io.apiman.osgi.pax.testing.util.HttpTestClient;
import io.apiman.osgi.pax.testing.util.KarafBaseTest;
import io.searchbox.indices.DeleteIndex;
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

import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Test service System/status of the Gateway
 */
@RunWith(PaxExam.class)
public class GatewayApiSystemStatusTest extends KarafBaseTest {

	private static final Logger LOG = LoggerFactory.getLogger(GatewayApiSystemStatusTest.class);
	private static final String EXPECTED_CONTENT = "{\"id\":\"apiman-gateway-api\",\"name\":\"API Gateway REST API\",\"description\":\"The API Gateway REST API is used by the API Manager to publish APIs and register clients.  You can use it directly if you wish, but if you are utilizing the API Manager then it's probably best to avoid invoking this API directly.\",\"version\":\"1.2.2-SNAPSHOT\",\"up\":true}";

	private Bundle warBundle;
	protected HttpTestClient testClient;
	private ElasticSearchEmbed es;

	@Configuration
	public Option[] config() {
		return combine(baseConfig());
	}

	@Test
	public void testSystemStatus() throws Exception {
		testClient.testWebPath("https://localhost:8444/apiman-gateway-api/system/status",EXPECTED_CONTENT,200,true);
	}

	@Before
	public void setUp() throws Exception {

		es = new ElasticSearchEmbed();
		es.launch();

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