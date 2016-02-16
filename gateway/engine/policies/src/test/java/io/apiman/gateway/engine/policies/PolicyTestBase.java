package io.apiman.gateway.engine.policies;

import org.mockito.Mockito;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * Base class for all policy tests cases.
 * 
 * @author Wojciech Trocki <wtr@redhat.com>
 */
public class PolicyTestBase {

    protected PolicyFailure createFailurePolicyObject(IPolicyContext context) {
        // Failure
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class))
                .thenReturn(new IPolicyFailureFactoryComponent() {
                    @Override
                    public PolicyFailure createFailure(PolicyFailureType type, int failureCode,
                            String message) {
                        return failure;
                    }
                });
        return failure;
    }
}
