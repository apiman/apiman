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

import java.util.List;

import org.overlord.apiman.rt.engine.ApimanBuffer;
import org.overlord.apiman.rt.engine.async.Abortable;
import org.overlord.apiman.rt.engine.async.IAsyncHandler;
import org.overlord.apiman.rt.engine.async.AbstractStream;
import org.overlord.apiman.rt.engine.async.IReadWriteStream;
import org.overlord.apiman.rt.engine.beans.PolicyFailure;

/**
 * Traverses and executes a series of policies according to implementor's
 * settings and iterator. This can be used to chain together and execute
 * policies in arbitrary order.
 *
 * The head handler is the first executed, arriving chunks are passed into
 * {@link #write(ApimanBuffer)}, followed by the {@link #end()} signal.
 * Intermediate policies handlers are chained together, according to the
 * ordering provided by {@link #policyIterator()}.
 *
 * The tail handler is executed last: the result object ({@link #getHead()} is
 * sent to {@link #handleHead(Object)}; chunks are streamed to out
 * {@link #handleBody(ApimanBuffer)}; end of transmission indicated via
 * {@link #handleEnd()}.
 *
 * @author Marc Savy <msavy@redhat.com>
 *
 * @param <H> Head type
 */
public abstract class Chain<H> extends AbstractStream<H> implements Abortable {

    protected final List<AbstractPolicy> policies;
    protected final IPolicyContext context;

    protected IReadWriteStream<H> headPolicyHandler;
    protected IReadWriteStream<H> tailPolicyHandler;
    protected IAsyncHandler<PolicyFailure> policyFailureHandler;
    protected IAsyncHandler<Throwable> policyErrorHandler;

    protected int startIndex;
    protected H serviceObject;

    public Chain(List<AbstractPolicy> policies, IPolicyContext context, int startIndex) {
        this.policies = policies;
        this.context = context;
        this.startIndex = startIndex;
    }

    protected void chainPolicyHandlers() {
        IReadWriteStream<H> previousHandler = null;
        ResettableIterator<AbstractPolicy> iterator = policyIterator();

        while (iterator.hasNext()) {
            final AbstractPolicy policy = iterator.next();

            if (previousHandler != null) {

                previousHandler.bodyHandler(new IAsyncHandler<ApimanBuffer>() {

                    @Override
                    public void handle(ApimanBuffer result) {
                        getServiceHandler(policy).write(result);
                    }
                });

                previousHandler.endHandler(new IAsyncHandler<Void>() {

                    @Override
                    public void handle(Void result) {
                        getServiceHandler(policy).end();
                    }

                });
            }

            previousHandler = getServiceHandler(policy);
        }

        tailPolicyHandler.bodyHandler(new IAsyncHandler<ApimanBuffer>() {

            @Override
            public void handle(ApimanBuffer chunk) {
                handleBody(chunk);
            }
        });

        tailPolicyHandler.endHandler(new IAsyncHandler<Void>() {

            @Override
            public void handle(Void result) {
                handleEnd();
            }
        });

        iterator.reset();
    }

    public void doApply(H serviceObject) {
        try {
            this.serviceObject = serviceObject;
            if (policyIterator().hasNext()) {
                executePolicy(policyIterator().next());
            } else {
                handleHead(getHead());
            }
        } catch (Throwable error) {
            throwError(error);
        }
    }

    @Override
    public void write(ApimanBuffer chunk) {
        if (finished) {
            throw new IllegalStateException("Attempted write after #end() was called."); //$NON-NLS-1$
        }

        headPolicyHandler.write(chunk);
    }

    @Override
    public void end() {
        headPolicyHandler.end();
    }

    @Override
    public H getHead() {
        return serviceObject;
    }

    @Override
    protected void handleHead(H service) {
        if (headHandler != null)
            headHandler.handle(service);
    }

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
        for (AbstractPolicy policy : policies) {
            policy.abort();
        }
    }

    protected abstract IReadWriteStream<H> getServiceHandler(AbstractPolicy policy);

    protected abstract ResettableIterator<AbstractPolicy> policyIterator();

    protected abstract void executePolicy(AbstractPolicy policy);

}
