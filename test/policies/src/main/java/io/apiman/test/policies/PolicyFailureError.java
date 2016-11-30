/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.test.policies;

import io.apiman.gateway.engine.beans.PolicyFailure;

/**
 * Thrown when the policy test results in a policy failure.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyFailureError extends Exception {

    private static final long serialVersionUID = -1704709451818184865L;

    private final PolicyFailure failure;

    /**
     * Constructor.
     * @param failure
     */
    public PolicyFailureError(PolicyFailure failure) {
        super(failure.getMessage());
        this.failure = failure;
    }

    /**
     * @return the failure
     */
    public PolicyFailure getFailure() {
        return failure;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PolicyFailureError [failure=" + failure + "]";
    }
}
