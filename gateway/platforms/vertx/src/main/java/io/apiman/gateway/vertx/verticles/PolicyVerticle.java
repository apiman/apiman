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
package io.apiman.gateway.vertx.verticles;

import io.apiman.gateway.vertx.config.VertxEngineConfig;
import io.apiman.gateway.vertx.conversation.ServiceRequestListener;
import io.apiman.gateway.vertx.conversation.ServiceResponseExecutor;
import io.apiman.gateway.vertx.io.VertxApimanBuffer;
import io.apiman.gateway.vertx.worker.WorkerHelper;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IServiceRequestExecutor;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalWriteStream;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;

/**
 * Verticle responsible for executing policies and returning result.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class PolicyVerticle extends ApimanVerticleWithEngine {
    protected WorkerHelper worker;
    private ServiceRequestListener requestHandler;
    private ServiceResponseExecutor responseExecutor;

    @Override
    public void start() {
        super.start();
        worker = new WorkerHelper(VertxEngineConfig.APIMAN_RT_EP_GATEWAY_REG_POLICY, uuid, eb, logger);

        requestHandler = new ServiceRequestListener(eb, logger, worker.getUuid() + VertxEngineConfig.APIMAN_RT_EP_SERVICE_REQUEST);
        responseExecutor = new ServiceResponseExecutor(eb, logger, worker.getUuid() + VertxEngineConfig.APIMAN_RT_EP_SERVICE_RESPONSE);

        setup();
    }

    private void setup() {
        requestHandler();

        if(config.containsField("skip_registration") && config.getBoolean("skip_registration")) {
            logger.debug(uuid + " is not registering (skip_registration flag).");
        } else {
            register();
        }
    }

    private void register() {
        worker.register(new AsyncResultHandler<Message<String>>() {

            @Override
            public void handle(AsyncResult<Message<String>> result) {
                if (result.failed()) {
                    ReplyException failure = (ReplyException) result.cause();
                    logger.error("Unable to register: " + failure.failureType()); //$NON-NLS-1$
                    logger.error("Failure code: " + failure.failureCode()); //$NON-NLS-1$
                    logger.error("Failure message: " + failure.getMessage()); //$NON-NLS-1$
                }
            }
        });
    }

    private void requestHandler() {
        requestHandler.serviceHandler(new Handler<ServiceRequest>() {

            @Override
            public void handle(ServiceRequest request) {
                logger.debug("Received a new ServiceRequest!");

                try {
                    doRequest(request);
                }  catch (Throwable e) {
                    responseExecutor.error(e);
                }
            }
        });

        requestHandler.listen();
    }

    private void doRequest(ServiceRequest request) {

        final IServiceRequestExecutor requestExecutor = engine.executor(request, new IAsyncResultHandler<IEngineResult>() {

            @Override
            public void handle(IAsyncResult<IEngineResult> result) {
                logger.debug("received result!");

                if (result.isSuccess()) {
                    IEngineResult engineResult = result.getResult();

                    if (engineResult.isResponse()) {
                        handleSuccessfulResponse(engineResult);
                    } else {
                        responseExecutor.failure(engineResult.getPolicyFailure());
                        reset();
                    }

                } else {
                    responseExecutor.error(result.getError());
                    reset();
                }
            }

            private void handleSuccessfulResponse(IEngineResult engineResult) {

                engineResult.bodyHandler(new IAsyncHandler<IApimanBuffer>() {

                    @Override
                    public void handle(IApimanBuffer chunk) {
                        responseExecutor.write((Buffer) chunk.getNativeBuffer());
                    }
                });

                engineResult.endHandler(new IAsyncHandler<Void>() {

                    @Override
                    public void handle(Void flag) {
                        responseExecutor.end();
                        reset();
                    }
                });

                responseExecutor.writeResponse(engineResult.getServiceResponse());
            }
        });

        // apiman is ready to receive chunks.
        requestExecutor.streamHandler(new IAsyncHandler<ISignalWriteStream>() {

            @Override
            public void handle(final ISignalWriteStream writeStream) {

                // Body handler for vert.x chunks.
                requestHandler.bodyHandler(new Handler<Buffer>() {

                    @Override
                    public void handle(Buffer chunk) {
                        writeStream.write(new VertxApimanBuffer(chunk));
                    }
                });

                // End handler for vert.x
                requestHandler.endHandler(new VoidHandler() {

                    @Override
                    protected void handle() {
                        //logger.debug("Sending end flag to ISignalWriteStream");
                        writeStream.end();
                    }
                });

                // Indicate that we're ready to receive chunks.
                requestHandler.ready();
            }
        });

        requestExecutor.execute();
    }

    // Reset finished flags. TODO verify.
    private void reset() {
        requestHandler.reset();
        responseExecutor.reset();
    }

    @Override
    public String verticleType() {
        return "policy";
    }

    public IEngine getEngine() {
        return engine;
    }
}
