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

import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.io.ISignalReadStream;

/**
 * A API connection response represents the result of a successful connection
 * to a back-end API.  The connection response is a {@link ApiResponse} object
 * as well as a stream of data (the response payload/body).
 *
 * One of these is passed asynchronously to apiman *after* the API connection is
 * established, all request body data is written, and the back-end API responds
 * successfully.
 *
 * The {@link ApiResponse} MUST be made available via the getHead() method and
 * should be available immediately.
 *
 * The response payload/body is delivered asynchronously.  apiman will add asynchronous
 * body and end handlers to this connection response and then call transmit().  Once
 * transmit() is called, that is the signal to the implementation of this interface
 * to go ahead and start sending the data to the body handler.  Once all data is written,
 * an implementation MUST call the end handler.
 *
 * If an error occurs during data transmission, apiman WILL call the abort() method
 * of this interface, providing an opportunity for implementations to close down the
 * connection and free up any resources.
 *
 * There is no close() method, so an implementation is responsible for ensuring that
 * all of its resources are freed prior to calling the end handler.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IApiConnectionResponse extends ISignalReadStream<ApiResponse> {

}
