/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.apiman.rt.engine.policy;

import java.util.Iterator;
import java.util.List;

import org.overlord.apiman.rt.engine.async.AbstractStream;
import org.overlord.apiman.rt.engine.async.IAbortable;
import org.overlord.apiman.rt.engine.async.IAsyncHandler;
import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.io.IBuffer;
import org.overlord.apiman.rt.engine.io.IReadWriteStream;

/**
 * Traverses and executes a series of policies according to implementor's
 * settings and iterator. This can be used to chain together and execute
 * policies in arbitrary order.
 *
 * The head handler is the first executed, arriving chunks are passed into
 * {@link #write(IBuffer)}, followed by the {@link #end()} signal.
 * Intermediate policy handlers are chained together, according to the
 * ordering provided by {@link #policyIterator()}.
 *
 * The tail handler is executed last: the result object ({@link #getHead()} is
 * sent to {@link #handleHead(Object)}; chunks are streamed to out
 * {@link #handleBody(IBuffer)}; end of transmission indicated via
 * {@link #handleEnd()}.
 *
 * @author Marc Savy <msavy@redhat.com>
 *
 * @param <H> Head type
 */
public abstract class Chain<H> extends AbstractStream<H> implements IAbortable, IPolicyChain<H>, Iterable<IPolicy> {

    private final List<IPolicy> policies;
    private final IPolicyContext context;

    private IReadWriteStream<H> headPolicyHandler;
    private IAsyncHandler<PolicyFailure> policyFailureHandler;
    private IAsyncHandler<Throwable> policyErrorHandler;

    private Iterator<IPolicy> policyIterator;
    
    private H serviceObject;

    /**
     * Constructor.
     * @param policies
     * @param context
     */
    public Chain(List<IPolicy> policies, IPolicyContext context) {
        this.policies = policies;
        this.context = context;

        chainPolicyHandlers();
        policyIterator = iterator();
    }

    /**
     * Chain together the body handlers.
     */
    protected void chainPolicyHandlers() {
        IReadWriteStream<H> previousHandler = null;
        Iterator<IPolicy> iterator = iterator();
        while (iterator.hasNext()) {
            final IPolicy policy = iterator.next();
            final IReadWriteStream<H> handler = getServiceHandler(policy);
            if (handler == null) {
                continue;
            }
            
            if (headPolicyHandler == null) {
                headPolicyHandler = handler;
            }
            
            if (previousHandler != null) {
                previousHandler.bodyHandler(new IAsyncHandler<IBuffer>() {
                    @Override
                    public void handle(IBuffer result) {
                        handler.write(result);
                    }
                });
                previousHandler.endHandler(new IAsyncHandler<Void>() {
                    @Override
                    public void handle(Void result) {
                        handler.end();
                    }
                });
            }

            previousHandler = handler;
        }
        
        IReadWriteStream<H> tailPolicyHandler = previousHandler;
        
        // If no policy handlers were found, then just make ourselves the head,
        // otherwise connect the last policy handler in the chain to ourselves
        if (headPolicyHandler == null) {
            // Leave the head and tail policy handlers null - the write() and end() methods
            // will deal with that case.
        } else {
            tailPolicyHandler.bodyHandler(new IAsyncHandler<IBuffer>() {
    
                @Override
                public void handle(IBuffer chunk) {
                    handleBody(chunk);
                }
            });
    
            tailPolicyHandler.endHandler(new IAsyncHandler<Void>() {
    
                @Override
                public void handle(Void result) {
                    handleEnd();
                }
            });
        }
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicyChain#doApply(java.lang.Object)
     */
    @Override
    public void doApply(H serviceObject) {
        try {
            this.serviceObject = serviceObject;
            if (policyIterator.hasNext()) {
                applyPolicy(policyIterator.next(), context);
            } else {
                handleHead(getHead());
            }
        } catch (Throwable error) {
            throwError(error);
        }
    }

    /**
     * @see org.overlord.apiman.rt.engine.async.AbstractStream#write(org.overlord.apiman.rt.engine.io.IBuffer)
     */
    @Override
    public void write(IBuffer chunk) {
        if (finished) {
            throw new IllegalStateException("Attempted write after #end() was called."); //$NON-NLS-1$
        }
        
        if (headPolicyHandler != null) {
            headPolicyHandler.write(chunk);
        } else {
            handleBody(chunk);
        }
    }

    /**
     * @see org.overlord.apiman.rt.engine.async.AbstractStream#end()
     */
    @Override
    public void end() {
        if (headPolicyHandler != null) {
            headPolicyHandler.end();
        } else {
            handleEnd();
        }
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IReadStream#getHead()
     */
    @Override
    public H getHead() {
        return serviceObject;
    }

    /**
     * @see org.overlord.apiman.rt.engine.async.AbstractStream#handleHead(java.lang.Object)
     */
    @Override
    protected void handleHead(H service) {
        if (headHandler != null)
            headHandler.handle(service);
    }

    /**
     * Sets the policy failure handler.
     * @param failureHandler
     */
    public void policyFailureHandler(IAsyncHandler<PolicyFailure> failureHandler) {
        this.policyFailureHandler = failureHandler;
    }

    /**
     * Handle a policy failure.
     * 
     * @param failure the policy failure
     */
    public void doFailure(PolicyFailure failure) {
        abort();
        policyFailureHandler.handle(failure);
    }

    /**
     * Sets the policy error handler.
     * @param policyErrorHandler
     */
    public void policyErrorHandler(IAsyncHandler<Throwable> policyErrorHandler) {
        this.policyErrorHandler = policyErrorHandler;
    }

    /**
     * Called when an unexpected and unrecoverable error is encountered.
     * 
     * @param error
     */
    public void throwError(Throwable error) {
        abort();
        policyErrorHandler.handle(error);
    }

    /**
     * Send abort signal to all policies.
     */
    public void abort() {
//        for (IPolicy policy : policies) {
//            policy.abort();
//        }
    }

    /**
     * Gets the service handler for the policy.
     * @param policy
     */
    protected abstract IReadWriteStream<H> getServiceHandler(IPolicy policy);

    /**
     * Called to apply the given policy to the service object (request or response).
     * @param policy
     * @param context
     */
    protected abstract void applyPolicy(IPolicy policy, IPolicyContext context);

    /**
     * @return the policies
     */
    public List<IPolicy> getPolicies() {
        return policies;
    }

}
