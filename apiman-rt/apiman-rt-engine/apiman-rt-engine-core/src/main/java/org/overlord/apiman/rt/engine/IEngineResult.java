package org.overlord.apiman.rt.engine;

import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.io.IReadStream;

public interface IEngineResult extends IReadStream<ServiceResponse>  {

    /**
     * Whether a response has been set.
     * 
     * @return true if response set, false if unset.
     */
    public abstract boolean isResponse();

    /**
     * Whether a failure occurred during engine execution.
     * 
     * @return true if failure set, false if unset.
     */
    public abstract boolean isFailure();

    /**
     * @return the serviceResponse
     */
    public abstract ServiceResponse getServiceResponse();

    /**
     * @return the policyFailure
     */
    public abstract PolicyFailure getPolicyFailure();

}