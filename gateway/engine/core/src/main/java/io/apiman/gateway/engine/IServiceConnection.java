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
package io.apiman.gateway.engine;

import io.apiman.gateway.engine.io.ISignalWriteStream;

/**
 * A connection to a back-end service.  This connection is initiated by apiman when
 * an inbound service request has passed all of the inbound policies and needs to be
 * proxied to the back-end service.
 *
 * Consumers of an IServiceConnection are simply responsible for writing all of the
 * inbound request's body data (if any) to the connection.
 *
 * If the inbound request has data, then the write() method should be called
 * repeatedly for all of the data in the request.
 *
 * If an error of some kind occurs while writing the data, then the abort() method
 * MUST be called.
 *
 * Once all request payload data has been written (or if no data is available e.g.
 * for GET requests) then the end() method MUST bve called.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IServiceConnection extends ISignalWriteStream {

    /**
     * Returns true if the connection has been made successfully.
     * @return true if is connected
     */
    boolean isConnected();

}
