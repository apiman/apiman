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
package io.apiman.gateway.test.junit.vertx3;

/**
 * Reset a gateway datastore that is under testing to ensure clean environment
 * between test groups which may not be validly executed serially.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface Resetter {

    /**
     * Reset the data store. Should block until the reset is completed.
     */
    void reset();
}
