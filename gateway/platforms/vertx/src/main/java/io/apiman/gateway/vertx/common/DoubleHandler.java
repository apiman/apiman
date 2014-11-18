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
package io.apiman.gateway.vertx.common;

/**
 * Handler that returns two elements of different types.
 * 
 * Use very sparingly.
 * 
 * @author Marc Savy <msavy@redhat.com>
 *
 * @param <T1> First element returned
 * @param <T2> Second element returned
 */
public interface DoubleHandler<T1, T2> {
    /**
     * Handle elements
     * 
     * @param elem0 First element
     * @param elem1 Second element
     */
    void handle(T1 elem0, T2 elem1);
}
