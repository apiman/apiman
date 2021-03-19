/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.gateway.test.policies;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.ApimanLoggerFactoryRegistry;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.test.logging.LogTestDelegateFactory;
import io.apiman.gateway.test.logging.TestLogger;

/**
 * A simple policy used for testing.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class LoggingPolicy implements IPolicy {

    public static void reset() {
    }

    /**
     * Constructor.
     */
    public LoggingPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     */
    @Override
    public Object parseConfiguration(String jsonConfiguration) {
        return new Object();
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    @SuppressWarnings("nls")
    public void apply(final ApiRequest request, final IPolicyContext context, final Object config,
            final IPolicyChain<ApiRequest> chain) {
        // This is just for testing, don't do this at home, kids.
        // Depending how you run the tests, the logger may already have been resolved so it will fail.
        // To combat this, we set the delegate manually here.
        ApimanLoggerFactory.setDelegate(new LogTestDelegateFactory());
        TestLogger logger = (TestLogger) context.getLogger(getClass());
        logger.setHeaders(request.getHeaders());
        // These messages are written as headers into request.getHeaders(), so we can assert against them
        // via the REST test suite. 
        logger.info("Hello, I am an info message");
        logger.debug("Hello, I am a debug message");
        logger.warn("Hello, I am a warn message");
        logger.trace("Hello, I am a trace message");
        logger.error("Hello, I am an error message", new RuntimeException("An example of an error"));
        logger.error(new RuntimeException("Just the exception"));
        chain.doApply(request);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ApiResponse response, IPolicyContext context, Object config,
            IPolicyChain<ApiResponse> chain) {
        chain.doApply(response);
    }

}
