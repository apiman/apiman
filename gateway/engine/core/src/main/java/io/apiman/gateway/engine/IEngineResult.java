package io.apiman.gateway.engine;

import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.io.IAbortable;
import io.apiman.gateway.engine.io.IReadStream;

/**
 * This is what is passed to the original caller when the engine wants to return either
 * a successful {@link ApiResponse} or a {@link PolicyFailure}.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IEngineResult extends IReadStream<ApiResponse>, IAbortable  {

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
     * @return the apiResponse
     */
    public ApiResponse getApiResponse();

    /**
     * @return the policyFailure
     */
    public PolicyFailure getPolicyFailure();

}
