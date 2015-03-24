package io.apiman.gateway.engine;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.impl.ServiceRequestExecutorImpl;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.engine.io.ITransportSecurityStatus;
import io.apiman.gateway.engine.io.ITransportSecurityStatusSet;

/**
 * IPolicyRequestExecutor interface.
 * 
 * @author Marc Savy <msavy@redhat.com>
 *
 * @see ServiceRequestExecutorImpl
 */
public interface IServiceRequestExecutor {

    /**
     * Execute the policy chain and request.
     */
    void execute();

    /**
     * Policy request-response sequence has completed.
     * 
     * @return true if finished, else false.
     */
    boolean isFinished();

    /**
     * Indicates when the back-end connector is ready to handle streamed data.
     *
     * @param handler
     */
    void streamHandler(IAsyncHandler<ISignalWriteStream> handler);
}