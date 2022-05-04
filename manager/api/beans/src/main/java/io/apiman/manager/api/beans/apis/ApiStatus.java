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
package io.apiman.manager.api.beans.apis;

/**
 * The various API statuses.
 *
 * @author eric.wittmann@redhat.com
 */
public enum ApiStatus {

    /**
     * API has been created but is not yet fully configured.
     */
    Created,

    /**
     * API has been created and configured, and is ready for publication
     */
    Ready,

    /**
     * API has been published:
     * <ul>
     *     <li>If a public API: is published to a gateway.</li>
     *     <li>if a non-public API: can be subscribed to by clients via a plan.</li>
     * </ul>
     */
    Published,

    /**
     * API has been withdrawn
     */
    Retired

}
