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
package io.apiman.gateway.vertx.worker;

import org.vertx.java.core.Handler;

/**
 * A registrant, generally a node which is registered with a queue.
 * 
 * A listener of any type may be associated with it, which may be useful if the listeners can be reused.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public interface Registrant {
    /**
     * @return Address of registrant
     */
    String getAddress();
    
    /**
     * When completed.
     * 
     * @param completedHandler
     */
    void endHandler(Handler<Void> completedHandler);
}
