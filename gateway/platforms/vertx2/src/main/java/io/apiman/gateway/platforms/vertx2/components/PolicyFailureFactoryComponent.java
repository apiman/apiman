package io.apiman.gateway.platforms.vertx2.components;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;

public class PolicyFailureFactoryComponent implements IPolicyFailureFactoryComponent {
    
    /**
     * Constructor.
     */
    public PolicyFailureFactoryComponent() {
    }

    /**
     * @see io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent#createFailure(io.apiman.gateway.engine.beans.PolicyFailureType, int, java.lang.String)
     */
    @Override
    public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
        PolicyFailure failure = new PolicyFailure(); // TODO pool
        failure.setFailureCode(failureCode);
        failure.setMessage(message);
        failure.setType(type);
        return failure;
    }
}
