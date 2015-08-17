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
package io.apiman.gateway.engine.vertxebinmemory.services;

import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Service;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;

/**
 * Publishes mutating events onto the event bus for listeners to consume. A UUID is sent to avoid circular
 * calls.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class EBRegistryProxy {
    Vertx vertx;
    DeliveryOptions options;
    String address;
    private String uuid;

    public static final String REGISTER = "register"; //$NON-NLS-1$
    public static final String UNREGISTER = "unregister"; //$NON-NLS-1$
    public static final String PUBLISH = "publish"; //$NON-NLS-1$
    public static final String RETIRE = "retire"; //$NON-NLS-1$

    public EBRegistryProxy(Vertx vertx, String address, String uuid) {
        this.vertx = vertx;
        this.address = address;
        this.uuid = uuid;
    }

    public void registerApplication(Application application) {
        vertx.eventBus().publish(address, new VxApplication(application, REGISTER, uuid).asJson());
    }

    public void unregisterApplication(Application application) {
        vertx.eventBus().publish(address, new VxApplication(application, UNREGISTER, uuid).asJson());
    }

    public void publishService(Service service) {
        System.out.println("publishing service on " + address); //$NON-NLS-1$
        vertx.eventBus().publish(address, new VxService(service, PUBLISH, uuid).asJson());
    }

    public void retireService(Service service) {
        vertx.eventBus().publish(address, new VxService(service, RETIRE, uuid).asJson());
    }
}
