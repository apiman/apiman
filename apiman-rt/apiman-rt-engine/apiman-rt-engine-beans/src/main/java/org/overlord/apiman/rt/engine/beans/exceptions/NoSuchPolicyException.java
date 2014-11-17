/*
 * Copyright 2013 JBoss Inc
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

package org.overlord.apiman.rt.engine.beans.exceptions;

/**
 * No such policy exists to be loaded.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class NoSuchPolicyException extends AbstractEngineException {


    /**
     * 
     */
    private static final long serialVersionUID = -5290745134121304201L;

    /**
     * Constructor.
     * @param componentType
     */
    public NoSuchPolicyException(String componentType) {
        super("No such policy loaded: " + componentType); //$NON-NLS-1$
    }

    /**
     * Constructor.
     * @param componentType
     * @param cause the root cause
     */
    public NoSuchPolicyException(String componentType, Throwable cause) {
        super("No such policy loaded: " + componentType, cause); //$NON-NLS-1$
    }
}
