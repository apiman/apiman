/*
 * Copyright 2018 JBoss Inc
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

package io.apiman.gateway.engine.policy;

import io.apiman.gateway.engine.beans.PolicyFailure;

/**
 * For exposing only failure elements of policy chain.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public interface IPolicyFailureChain {
    /**
     * Called by a policy when it has detected a violation or failure in the policy.  This
     * will stop the processing of policies and immediately return an failure response to
     * the originating client.
     * @param failure the policy failure
     */
    public void doFailure(PolicyFailure failure);
}
