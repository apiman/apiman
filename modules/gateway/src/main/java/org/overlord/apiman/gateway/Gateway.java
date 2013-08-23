/*
 * 2012-3 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.apiman.gateway;

import org.overlord.apiman.Request;
import org.overlord.apiman.Response;

/**
 * This interface represents a gateway responsible for authenticating
 * requests, applying relevant policies and forwarding the request
 * to a target service.
 *
 */
public interface Gateway {

	/**
	 * This method processes the supplied request and returns a response.
	 * 
	 * @param request The request
	 * @return The response
	 * @throws Exception Failed to process request
	 */
	public Response process(Request request) throws Exception;
	
}
