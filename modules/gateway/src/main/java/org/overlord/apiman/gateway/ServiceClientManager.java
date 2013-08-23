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

/**
 * This interface is for the service client manager, responsible for
 * determining which service client should be used to handle a gateway
 * request.
 *
 */
public interface ServiceClientManager {

	/**
	 * This method returns the service client appropriate for the
	 * supplied request.
	 * 
	 * @param request The request
	 * @return The service client
	 */
	public ServiceClient getServiceClient(Request request);
	
}
