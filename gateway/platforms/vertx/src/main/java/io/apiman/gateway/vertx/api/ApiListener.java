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
package io.apiman.gateway.vertx.api;

import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.vertx.config.VertxEngineConfig;
import io.netty.handler.codec.http.HttpResponseStatus;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;

/**
 * Listens for api updates.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class ApiListener {
    private EventBus eb;
    private String uuid;

    /**
     * Constructor
     *
     * @param eb entity bus
     * @param uuid verticle's unique id
     */
    public ApiListener(EventBus eb, String uuid) {
        this.eb = eb;
        this.uuid = uuid;
    }

    public void listen(final IEngine engine) {
        // Subscribe to the api
        eb.send(VertxEngineConfig.APIMAN_API_SUBSCRIBE, uuid);

        // register application
        eb.registerHandler(uuid + VertxEngineConfig.APIMAN_API_APPLICATIONS_REGISTER,
                new ApiCatchHandler<String>() {

            @Override
            protected void handleApi(final Message<String> message) {
                Application application = Json.decodeValue(message.body(), Application.class);
                engine.getRegistry().registerApplication(application, new IAsyncResultHandler<Void>() {
                    @Override
                    public void handle(IAsyncResult<Void> result) {
                        if (result.isSuccess()) {
                            // Signal that everything seems OK
                            replyOk(message);
                        } else {
                            Throwable e = result.getError();
                            replyError(message, new GenericError(HttpResponseStatus.NOT_FOUND.code(),
                                    e.getLocalizedMessage(), e));
                        }
                    }
                });
                //logger.debug(("registered app " + application.getApplicationId());
                //logger.debug(("with contract(s): ");

                // for(Contract c : application.getContracts()) {
                //     logger.debug(("API KEY = "  + c.getApiKey());
                // }
            };
        });

        // delete application
        eb.registerHandler(uuid + VertxEngineConfig.APIMAN_API_APPLICATIONS_DELETE,
                new ApiCatchHandler<JsonObject>() {

            @Override
            protected void handleApi(final Message<JsonObject> message) {
                JsonObject json = message.body();
                String orgId = json.getString("organizationId"); //$NON-NLS-1$
                String appId = json.getString("applicationId"); //$NON-NLS-1$
                String version = json.getString("version"); //$NON-NLS-1$
                Application app = new Application();
                app.setOrganizationId(orgId);
                app.setApplicationId(appId);
                app.setVersion(version);
                engine.getRegistry().unregisterApplication(app, new IAsyncResultHandler<Void>() {
                    @Override
                    public void handle(IAsyncResult<Void> result) {
                        if (result.isSuccess()) {
                            replyOk(message);
                        } else {
                            Throwable e = result.getError();
                            replyError(message, new GenericError(HttpResponseStatus.NOT_FOUND.code(),
                                    e.getLocalizedMessage(), e));
                        }
                    }
                });
                //logger.debug(("Deleted app");
            };
        });

        // register service
        eb.registerHandler(uuid + VertxEngineConfig.APIMAN_API_SERVICES_REGISTER,
                new ApiCatchHandler<String>() {

            @Override
            public void handleApi(final Message<String> message) {
                Service service = Json.decodeValue(message.body(), Service.class);
                engine.getRegistry().publishService(service, new IAsyncResultHandler<Void>() {
                    @Override
                    public void handle(IAsyncResult<Void> result) {
                        if (result.isSuccess()) {
                            replyOk(message);
                        } else {
                            Throwable e = result.getError();
                            replyError(message, new GenericError(HttpResponseStatus.CONFLICT.code(),
                                    e.getLocalizedMessage(), e));
                        }
                    }
                });
                //logger.debug(("registered service " + service.getEndpointType());
            };
        });

        // retire service
        eb.registerHandler(uuid + VertxEngineConfig.APIMAN_API_SERVICES_DELETE,
                new ApiCatchHandler<JsonObject>() {

            @Override
            public void handleApi(final Message<JsonObject> message) {
                JsonObject json = message.body();
                String orgId = json.getString("organizationId"); //$NON-NLS-1$
                String svcId = json.getString("serviceId"); //$NON-NLS-1$
                String version = json.getString("version"); //$NON-NLS-1$
                Service service = new Service();
                service.setOrganizationId(orgId);
                service.setServiceId(svcId);
                service.setVersion(version);
                engine.getRegistry().retireService(service, new IAsyncResultHandler<Void>() {
                    @Override
                    public void handle(IAsyncResult<Void> result) {
                        if (result.isSuccess()) {
                            replyOk(message);
                        } else {
                            Throwable e = result.getError();
                            replyError(message, new GenericError(HttpResponseStatus.NOT_FOUND.code(),
                                    e.getLocalizedMessage(), e));
                        }
                    }
                });
            };
        });
    }
}
