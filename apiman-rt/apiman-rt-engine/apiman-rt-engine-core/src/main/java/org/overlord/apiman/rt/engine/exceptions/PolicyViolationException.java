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
package org.overlord.apiman.rt.engine.exceptions;

/**
 * Exception thrown when a policy is violated.  This happens during processing
 * of an inbound request to a managed service.  The policies associated with
 * the managed service are evaluated against the inbound request (and potentially
 * current state information).  If any policy is violated, an error of this type
 * will be raised.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyViolationException extends Exception {
    
    private static final long serialVersionUID = -456431090386043376L;

    /**
     * Constructor.
     * @param message an error message
     */
    public PolicyViolationException(String message) {
        super(message);
    }

}
