/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.gateway.platforms.vertx3.components;

import io.apiman.gateway.engine.components.IExecuteBlockingComponent;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
@RunWith(VertxUnitRunner.class)
public class ExecuteBlockingComponentTest {
    IExecuteBlockingComponent executeBlockingComponent = new ExecuteBlockingComponentImpl(Vertx.vertx(), null, null);

    @Test
    public void testSuccessfulExecution(TestContext context) {
        Async async = context.async();

        executeBlockingComponent.executeBlocking(future -> {
            future.completed("The Farquhar Islands");
        },
        result -> {
            context.assertTrue(result.isSuccess());
            context.assertFalse(result.isError());
            context.assertEquals(result.getResult(), "The Farquhar Islands");
            context.assertNull(result.getError());
            async.complete();
        });
    }

    @Test
    public void testFailedExecution(TestContext context) {
        Async async = context.async();

        executeBlockingComponent.executeBlocking(future -> {
            future.fail(new ExampleException("Silhoutte"));
        },
        result -> {
            context.assertFalse(result.isSuccess());
            context.assertTrue(result.isError());
            context.assertTrue(result.getError() instanceof ExampleException);
            context.assertEquals(result.getError().getMessage(), "Silhoutte");
            context.assertNull(result.getResult());
            async.complete();
        });
    }

    @Test
    public void testExceptionInExecuteBlock(TestContext context) {
        Async async = context.async();

        executeBlockingComponent.executeBlocking(future -> {
            throw new ExampleException("Desroches");
        },
        result -> {
            context.assertFalse(result.isSuccess());
            context.assertTrue(result.isError());
            context.assertTrue(result.getError() instanceof ExampleException);
            context.assertEquals(result.getError().getMessage(), "Desroches");
            context.assertNull(result.getResult());
            async.complete();
        });
    }

    private static final class ExampleException extends RuntimeException {
        private static final long serialVersionUID = 517125905991693943L;

        public ExampleException(String message) {
            super(message);
        }
    }
}
