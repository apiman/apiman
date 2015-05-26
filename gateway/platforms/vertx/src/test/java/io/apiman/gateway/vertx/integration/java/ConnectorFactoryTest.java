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
package io.apiman.gateway.vertx.integration.java;



import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;
import io.apiman.gateway.engine.IServiceConnection;
import io.apiman.gateway.engine.IServiceConnectionResponse;
import io.apiman.gateway.engine.IServiceConnector;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.HeaderHashMap;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.vertx.connector.ConnectorFactory;
import io.apiman.gateway.vertx.io.VertxApimanBuffer;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.logging.Logger;
import org.vertx.testtools.TestVerticle;

/**
 * Test connector factory using a dummy server on localhost that echoes a fixed response.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class ConnectorFactoryTest extends TestVerticle {

    private ConnectorFactory factory;
    private ServiceRequest mRequest;
    private Service mService;
    private HeaderHashMap headers;
    private Logger logger;
    private IApimanBuffer bTransmitBuffer;

    @Mock private IAsyncHandler<IApimanBuffer> mBodyHandler;
    @Mock private Handler<Buffer> mEchoDataHandler;

    public void before() {
        MockitoAnnotations.initMocks(this);

        logger = container.logger();

        factory = new ConnectorFactory(vertx, container);

        headers = spy(new HeaderHashMap());
        headers.put("zaphod", "beeblebrox");

        mRequest = mock(ServiceRequest.class);
        given(mRequest.getType()).willReturn("GET");
        given(mRequest.getDestination()).willReturn("/");
        given(mRequest.getHeaders()).willReturn(headers);

        mService = mock(Service.class);
        given(mService.getEndpoint()).willReturn("http://localhost:8999/");

        bTransmitBuffer = new VertxApimanBuffer(new Buffer("hitchhiker"));
    }

    @Test
    public void testHttpConnector() {
        before();

        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {

            @Override
            public void handle(final HttpServerRequest req) {
                assertEquals(req.headers().get("zaphod"), "beeblebrox");

                req.dataHandler(mEchoDataHandler);

                // We can't do any of our assertions until the end handler, else they are hit too early.
                req.endHandler(new VoidHandler() {
                    @Override
                    protected void handle() {
                        ArgumentCaptor<Buffer> buffCaptor = ArgumentCaptor.forClass(Buffer.class);
                        verify(mEchoDataHandler).handle(buffCaptor.capture());

                        assertEquals("hitchhiker", buffCaptor.getValue().toString());
                        assertEquals("beeblebrox", req.headers().get("zaphod"));

                        // Do the baked response.
                        req.response().setChunked(true);
                        req.response().setStatusCode(200);
                        req.response().putHeader("vogon", "bypass");
                        req.response().setStatusMessage("everything-is-fine");
                        req.response().write("lipwig");
                        req.response().end();
                    }
                });
            }
        }).listen(8999, new AsyncResultHandler<HttpServer>() {

            @Override
            public void handle(AsyncResult<HttpServer> event) {
                logger.info("Dummy server is listening. Testing proceeding:");

                assertTrue(event.succeeded());

                IServiceConnector connector = factory.createConnector(mRequest, mService, RequiredAuthType.DEFAULT);
                IServiceConnection writeStream = connector.connect(mRequest,
                        new IAsyncResultHandler<IServiceConnectionResponse>() {

                    @Override
                    public void handle(IAsyncResult<IServiceConnectionResponse> result) {
                        logger.info("Received a result.");

                        assertTrue(result.isSuccess());

                        IServiceConnectionResponse stream = result.getResult();

                        assertEquals(200, stream.getHead().getCode());
                        assertEquals("everything-is-fine", stream.getHead().getMessage());
                        assertEquals("bypass", stream.getHead().getHeaders().get("vogon"));

                        stream.bodyHandler(mBodyHandler);

                        stream.endHandler(new IAsyncHandler<Void>() {

                            @Override
                            public void handle(Void signal) {
                                ArgumentCaptor<IApimanBuffer> buffCaptor = ArgumentCaptor.forClass(IApimanBuffer.class);
                                verify(mBodyHandler).handle(buffCaptor.capture());
                                assertEquals("lipwig", buffCaptor.getValue().toString());

                                testComplete();
                            }
                        });

                        // It's okay to send body chunks now (real usage more sophisticated than this!).
                        stream.transmit();
                    }
                });

                writeStream.write(bTransmitBuffer);
                writeStream.end();
            }
        });
    }
}
