package io.apiman.osgi.pax.testing;

import io.apiman.osgi.pax.testing.util.GatewayRestTestPlan;
import io.apiman.osgi.pax.testing.util.KarafConfiguration;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExamServer;

@RunWith(GatewayRestOSGITester.class)
@GatewayRestTestPlan("test-plans/api/api-testPlan.xml")
public class SimpleKarafTest {
}
