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
package io.apiman.manager.api.beans.clients;

/**
 * The various client statuses.
 *
 * @author eric.wittmann@redhat.com
 */
public enum ClientStatus {

    /**
     * Client has been created but is not yet fully configured.
     */
    Created,

    /**
     * Client has been created and configured, and is ready for publication.
     */
    Ready,

    /**
     * If approval is required, then the client must wait until someone with appropriate permissions promotes the client
     * to the {@link #Registered} state.
     * <p>
     * How this state transition is achieved could vary considerably depending on how Apiman is configured (e.g. an out
     * of band business process, a human interaction, etc.).
     */
    AwaitingApproval,

    /**
     * Client has been registered and its configuration has been pushed to a gateway. This means traffic can be routed
     * via to the client's API key, for example.
     */
    Registered,

    /**
     * The client has been permanently withdrawn and traffic can no longer be routed.
     */
    Retired

}
