package org.overlord.apiman.rt.engine;

import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.io.IAbortable;
import org.overlord.apiman.rt.engine.io.IReadStream;

/**
 * This is what is passed to the original caller when the engine wants to return either
 * a successful {@link ServiceResponse} or a {@link PolicyFailure}.
 * 
 * @author eric.wittmann@redhat.com
 */
public interface IEngineResult extends IReadStream<ServiceResponse>, IAbortable  {

    /**
     * Whether a response has been set.
     * 
     * @return true if response set, false if unset.
     */
    public boolean isResponse();

    /**
     * Whether a failure occurred during engine execution.
     * 
     * @return true if failure set, false if unset.
     */
    public boolean isFailure();

    /**
     * @return the serviceResponse
     */
    public ServiceResponse getServiceResponse();

    /**
     * @return the policyFailure
     */
    public PolicyFailure getPolicyFailure();

}