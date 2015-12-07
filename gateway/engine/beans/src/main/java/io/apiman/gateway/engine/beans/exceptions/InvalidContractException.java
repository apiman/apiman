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
package io.apiman.gateway.engine.beans.exceptions;

/**
 * Exception thrown when attempting to use a Contract in some invalid way.  For example
 * when trying to use a Contract for one API when accessing a different API.
 *
 * @author eric.wittmann@redhat.com
 */
public class InvalidContractException extends AbstractEngineException {

    private static final long serialVersionUID = -378275941461121749L;

    /**
     * Constructor.
     * @param message an error message
     */
    public InvalidContractException(String message) {
        super(message);
    }

}
