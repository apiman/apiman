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
package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.clients.ClientVersionBean;

/**
 * Validates the state of clients and client versions.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IClientValidator {

    /**
     * <p>
     * Is the given client Ready to be registered with the Gateway?  This method
     * will return true if all of the criteria for registration is met.  The
     * criteria includes (but is not necessarily limited to):
     * </p>
     *
     * <ul>
     *   <li>At least one Contract exists for the client</li>
     * </ul>
     *
     * @param client the client
     * @return true if ready, else false
     * @throws Exception exception
     */
    boolean isReady(ClientVersionBean client) throws Exception;

    /**
     * <p>
     * Is the given client Ready to be registered with the Gateway?  This method
     * will return true if all of the criteria for registration is met.  The
     * criteria includes (but is not necessarily limited to):
     * </p>
     *
     * <ul>
     *   <li>At least one Contract exists for the client</li>
     * </ul>
     *
     * <p>
     * This version of isRead() skips the check for contracts and instead
     * uses the value passed in.  This is important if, for example, a
     * contract is being created.
     * </p>
     *
     * @param client the clients
     * @param hasContracts whether the client has contracts
     * @return true if ready, else false
     * @throws Exception exception
     */
    boolean isReady(ClientVersionBean client, boolean hasContracts) throws Exception;

}
