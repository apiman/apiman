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
package io.apiman.manager.api.gateway.rest;

import io.apiman.gateway.engine.beans.exceptions.PublishingException;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link GatewayClient}.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class GatewayClientTest {

    private static final String EXPECTED_STACK = "io.apiman.gateway.engine.beans.exceptions.PublishingException: Error publishing!\r\n" +
            "\tat io.apiman.gateway.engine.es.ESRegistry$1.completed(ESRegistry.java:79)\r\n" +
            "\tat io.apiman.gateway.engine.es.ESRegistry$1.completed(ESRegistry.java:75)\r\n" +
            "\tat io.searchbox.client.http.JestHttpClient$DefaultCallback.completed(JestHttpClient.java:195)\r\n" +
            "\tat io.searchbox.client.http.JestHttpClient$DefaultCallback.completed(JestHttpClient.java:178)\r\n" +
            "\tat org.apache.http.concurrent.BasicFuture.completed(BasicFuture.java:119)\r\n" +
            "\tat org.apache.http.impl.nio.client.DefaultClientExchangeHandlerImpl.responseCompleted(DefaultClientExchangeHandlerImpl.java:177)\r\n" +
            "\tat org.apache.http.nio.protocol.HttpAsyncRequestExecutor.processResponse(HttpAsyncRequestExecutor.java:412)\r\n" +
            "\tat org.apache.http.nio.protocol.HttpAsyncRequestExecutor.inputReady(HttpAsyncRequestExecutor.java:305)\r\n" +
            "\tat org.apache.http.impl.nio.DefaultNHttpClientConnection.consumeInput(DefaultNHttpClientConnection.java:267)\r\n" +
            "\tat org.apache.http.impl.nio.client.InternalIODispatch.onInputReady(InternalIODispatch.java:81)\r\n" +
            "\tat org.apache.http.impl.nio.client.InternalIODispatch.onInputReady(InternalIODispatch.java:39)\r\n" +
            "\tat org.apache.http.impl.nio.reactor.AbstractIODispatch.inputReady(AbstractIODispatch.java:116)\r\n" +
            "\tat org.apache.http.impl.nio.reactor.BaseIOReactor.readable(BaseIOReactor.java:164)\r\n" +
            "\tat org.apache.http.impl.nio.reactor.AbstractIOReactor.processEvent(AbstractIOReactor.java:339)\r\n" +
            "\tat org.apache.http.impl.nio.reactor.AbstractIOReactor.processEvents(AbstractIOReactor.java:317)\r\n" +
            "\tat org.apache.http.impl.nio.reactor.AbstractIOReactor.execute(AbstractIOReactor.java:278)\r\n" +
            "\tat org.apache.http.impl.nio.reactor.BaseIOReactor.execute(BaseIOReactor.java:106)\r\n" +
            "\tat org.apache.http.impl.nio.reactor.AbstractMultiworkerIOReactor$Worker.run(AbstractMultiworkerIOReactor.java:590)\r\n" +
            "\tat java.lang.Thread.run(Thread.java:745)\r\n" +
            "";

    /**
     * Test method for {@link io.apiman.manager.api.gateway.rest.GatewayClient#parseStackTrace(java.lang.String)}.
     */
    @Test
    public void testParseStackTrace() {
        String stackTraceStr = "io.apiman.gateway.engine.beans.exceptions.PublishingException: API already published.\r\n\tat io.apiman.gateway.engine.es.ESRegistry$1.completed(ESRegistry.java:79)\r\n\tat io.apiman.gateway.engine.es.ESRegistry$1.completed(ESRegistry.java:75)\r\n\tat io.searchbox.client.http.JestHttpClient$DefaultCallback.completed(JestHttpClient.java:195)\r\n\tat io.searchbox.client.http.JestHttpClient$DefaultCallback.completed(JestHttpClient.java:178)\r\n\tat org.apache.http.concurrent.BasicFuture.completed(BasicFuture.java:119)\r\n\tat org.apache.http.impl.nio.client.DefaultClientExchangeHandlerImpl.responseCompleted(DefaultClientExchangeHandlerImpl.java:177)\r\n\tat org.apache.http.nio.protocol.HttpAsyncRequestExecutor.processResponse(HttpAsyncRequestExecutor.java:412)\r\n\tat org.apache.http.nio.protocol.HttpAsyncRequestExecutor.inputReady(HttpAsyncRequestExecutor.java:305)\r\n\tat org.apache.http.impl.nio.DefaultNHttpClientConnection.consumeInput(DefaultNHttpClientConnection.java:267)\r\n\tat org.apache.http.impl.nio.client.InternalIODispatch.onInputReady(InternalIODispatch.java:81)\r\n\tat org.apache.http.impl.nio.client.InternalIODispatch.onInputReady(InternalIODispatch.java:39)\r\n\tat org.apache.http.impl.nio.reactor.AbstractIODispatch.inputReady(AbstractIODispatch.java:116)\r\n\tat org.apache.http.impl.nio.reactor.BaseIOReactor.readable(BaseIOReactor.java:164)\r\n\tat org.apache.http.impl.nio.reactor.AbstractIOReactor.processEvent(AbstractIOReactor.java:339)\r\n\tat org.apache.http.impl.nio.reactor.AbstractIOReactor.processEvents(AbstractIOReactor.java:317)\r\n\tat org.apache.http.impl.nio.reactor.AbstractIOReactor.execute(AbstractIOReactor.java:278)\r\n\tat org.apache.http.impl.nio.reactor.BaseIOReactor.execute(BaseIOReactor.java:106)\r\n\tat org.apache.http.impl.nio.reactor.AbstractMultiworkerIOReactor$Worker.run(AbstractMultiworkerIOReactor.java:590)\r\n\tat java.lang.Thread.run(Thread.java:745)\r\n";

        StackTraceElement[] trace = GatewayClient.parseStackTrace(stackTraceStr);
        Assert.assertNotNull(trace);
        Assert.assertEquals(19, trace.length);

        PublishingException exception = new PublishingException("Error publishing!");
        exception.setStackTrace(trace);

        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String output = sw.toString();

        Assert.assertEquals(normalize(EXPECTED_STACK), normalize(output));
    }

    private static String normalize(String output) {
        return output.replaceAll("\r\n", "\n");
    }

}
