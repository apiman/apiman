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
package io.apiman.gateway.vertx.engine;

import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

/**
 * Test the vertx version of the plugin registry.
 */
@SuppressWarnings("nls")
public class VertxPluginRegistryTest extends TestVerticle {

    public void before() {
    }

    // TODO enable this test to do on-demand testing of the vertx plugin registry - don't enable by default since we don't want to depend on a remote server for unit tests
    @Test @Ignore
    public void testPluginRegistry() {
        VertxPluginRegistry registry = new VertxPluginRegistry(vertx);
        PluginCoordinates coords = new PluginCoordinates("io.apiman.plugins", "apiman-plugins-noop-policy", "1.1.1.Final", null, "war");
        registry.loadPlugin(coords, new IAsyncResultHandler<Plugin>() {
            @Override
            public void handle(IAsyncResult<Plugin> result) {
                Assert.assertTrue("Expected a successful response.", result.isSuccess());
                Assert.assertNotNull(result.getResult());
                Assert.assertEquals("No-op Policy Plugin", result.getResult().getName());
                System.out.println("Test passed.");
                VertxAssert.testComplete();
            }
        });
    }

}
