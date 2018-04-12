/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.gateway.platforms.vertx3.connector;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.platforms.vertx3.io.VertxApimanBuffer;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;

/**
 * <p>
 * Test to ensure drain handler fires as expected; when the write queue has been exceeded, and
 * thus advertises itself as full {@link ISignalWriteStream#isFull}. This enables a simple
 * back-pressure mechanism which enables the corresponding ingress point to be paused (thus
 * backing off) until load has decreased.
 * </p>
 * <p>
 * The server then calls {@link HttpServerRequest#resume()}, allowing the queued data to flow
 * through, simulating a scenario in which the backlog has been cleared. Once the queue has
 * sufficiently diminished, {@link ISignalWriteStream#drainHandler(IAsyncHandler)} will be
 * called to indicate that the client can begin sending again.
 * </p>
 * <p>
 * Order of operations:
 * <ul>
 * <li>HTTP server initiated which immediately {@link HttpServerRequest#pause()}es any received
 * requests.</li>
 * <li>Invoke {@link HttpConnector#write(IApimanBuffer)} on connector until it indicates the
 * queue {@link HttpConnector#isFull()}.</li>
 * <li>{@link HttpServerRequest#resume} HTTP server to consume (and thus reduce) incoming
 * queue.</li>
 * <li>{@link ISignalWriteStream#drainHandler(IAsyncHandler)} is invoked once queue is
 * sufficiently small (as determined by impl which at time of writing is maxSize/2).</li>
 * </ul>
 *
 * <p>
 * The drain handler is sometimes called multiple times, I think it's okay though.
 * </p>
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
@Ignore
@RunWith(VertxUnitRunner.class)
public class HttpConnectorDrainTest {

    private static final int WRITE_LIM = 1_000_000;
    final Api api = new Api();
    {
        api.setApiId("");
        api.setEndpoint("http://localhost:7297");
        api.setOrganizationId("");
        api.setParsePayload(false);
        api.setPublicAPI(true);
        api.setVersion("");
    }

    final ApiRequest request = new ApiRequest();
    {
        request.setApi(api);
        request.setApiId("");
        request.setApiKey("");
        request.setApiOrgId("");
        request.setApiVersion("");
        request.setDestination("/");
        request.setType("POST");
    }

    HttpServerRequest pausedRequest = null;
    HttpConnector httpConnector;

    @Before
    public void setup() {
    }

    @Test
    public void shouldTriggerDrainHandler(TestContext context) throws Exception {
        Async asyncDrain = context.async(2);
        Async asyncServer = context.async();
        Async waitForServer = context.async();
        Async receivedResponse = context.async();

        HttpServer server = Vertx.vertx().createHttpServer()
                .connectionHandler(connection -> {
                    System.out.println("Connection");
                })
                .requestHandler(requestToPause -> {
                    System.out.println("Test server: pausing inbound request!");
                    requestToPause.pause();
                    pausedRequest = requestToPause;
                    requestToPause
                            .handler(ignored -> {})
                            .endHandler(isEnded -> {
                                requestToPause.response().end("bye!");
                            });
                    asyncServer.complete();
                }).listen(7297, result -> waitForServer.complete());

        waitForServer.await();

        Vertx vertx = Vertx.vertx();
        httpConnector = new HttpConnector(vertx,
                vertx.createHttpClient(),
                request,
                api,
                new ApimanHttpConnectorOptions()
                    .setHasDataPolicy(true)
                    .setUri(URI.create(api.getEndpoint())),
                ignored -> {
                    // Immediately allow the dummy response to come back.
                    if (ignored.isError())
                        throw (RuntimeException) ignored.getError();
                    // Give permission to resume any response immediately.
                    httpConnector.transmit();
                });

        // Should be fired when write queue reduces to acceptable size.
        httpConnector.drainHandler(drain -> {
            System.err.println("Drain handler has been called! Yay.");
            asyncDrain.countDown();
        });

        httpConnector.bodyHandler(ignored -> {});

        httpConnector.endHandler(end -> {
            System.out.println("Called end on client. Should no longer be full!");
            receivedResponse.complete();
        });

        httpConnector.connect();

        writeUntilFull(httpConnector);

        Assert.assertTrue("Connection should be full", httpConnector.isFull());

        System.out.println("Waiting for server...");
        asyncServer.await();

        // Try to write some more as there *could* be some buffer flushed before the remote
        // server hit #pause.
        writeUntilFull(httpConnector);

        Assert.assertTrue("Connection should still be full", httpConnector.isFull());

        System.out.println("Resuming #pause()d server request; waiting packets should be consumed by the server.");
        pausedRequest.resume();

        System.out.println("Waiting for drain to be called (at least once)...");
        asyncDrain.await();

        Assert.assertFalse("Connector should no longer be full", httpConnector.isFull());

        System.out.println("Ending outbound connection");
        httpConnector.end();

        System.out.println("Waiting for response from server");
        receivedResponse.awaitSuccess();
    }

    private void writeUntilFull(HttpConnector httpConnector) {
        System.out.println("Writing until full. " +
                "Connected? " + httpConnector.isConnected() +
                " Full? " + httpConnector.isFull());
        int i;
        // Keep sending until stop is called (or reasonable upper bound.)
        for (i = 0; i < WRITE_LIM && !httpConnector.isFull(); i++) {
            httpConnector.write(new VertxApimanBuffer("Anonyme\n"
                    + "Aride\n"
                    + "Bird Island\n"
                    + "Cerf\n"
                    + "Chauve Souris\n"
                    + "Conception\n"
                    + "Cousin\n"
                    + "Cousine\n"
                    + "Curieuse\n"
                    + "Denis Island\n"
                    + "Frégate\n"
                    + "Félicité\n"
                    + "Grande Soeur\n"
                    + "Ile Cocos\n"
                    + "La Digue\n"
                    + "Long Island\n"
                    + "Mahé\n"
                    + "Moyenne\n"
                    + "North Island\n"
                    + "Petite Soeur\n"
                    + "Praslin\n"
                    + "Round Island\n"
                    + "Silhouette\n"
                    + "St. Pierre\n"
                    + "Ste. Anne"));
        }

        if (i == WRITE_LIM) {
            System.err.println("Sanity bound for #write met; something has likely gone wrong");
        }
    }

}
