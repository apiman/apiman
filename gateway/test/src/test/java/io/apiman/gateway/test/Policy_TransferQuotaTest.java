package io.apiman.gateway.test;

import io.apiman.gateway.test.junit.GatewayRestTestPlan;
import io.apiman.gateway.test.junit.GatewayRestTester;
import org.junit.runner.RunWith;

@RunWith(GatewayRestTester.class)
@GatewayRestTestPlan("test-plans/policies/transfer-quota-testPlan.xml")
public class Policy_TransferQuotaTest {


}
