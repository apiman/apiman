package org.overlord.apiman.rt.engine;

import org.overlord.apiman.rt.engine.async.IAsyncHandler;
import org.overlord.apiman.rt.engine.async.IWriteStream;

/**
 * IPolicyRequestExecutor interface.
 * 
 * @author Marc Savy <msavy@redhat.com>
 *
 * @see PolicyRequestExecutorImpl
 */
public interface IPolicyRequestExecutor {

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
    void streamHandler(IAsyncHandler<IWriteStream> handler);

}