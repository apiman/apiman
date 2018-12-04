/*
 * Copyright 2018 Pete Cornish
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
package io.apiman.gateway.engine.redis;

import io.apiman.gateway.engine.redis.common.RedisClientManager;
import io.apiman.gateway.engine.redis.support.TestRedisUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for {@link RedisSharedStateComponent}.
 *
 * @author Pete Cornish
 */
public class RedisSharedStateComponentTest {
    @Rule
    public GenericContainer redis = TestRedisUtil.buildRedisContainer();

    private RedisSharedStateComponent component;

    @Before
    public void setUp() {
        RedisClientManager.DEFAULT_MANAGER.reset();
        RedisClientManager.DEFAULT_MANAGER.setOverrideConfig(TestRedisUtil.buildComponentConfig(redis));
        component = new RedisSharedStateComponent(emptyMap());
    }

    @Test
    public void getSetAndClearProperty() {
        final String namespace = "namespace";
        final String propertyName = "propertyName";
        final String propertyValue = "propertyValue";

        component.setProperty(namespace, propertyName, propertyValue, result -> {
            if (null != result.getError()) {
                result.getError().printStackTrace();
            }
            assertFalse("The property should be set successfully", result.isError());
        });

        component.getProperty(namespace, propertyName, "defaultValue", result -> {
            if (null != result.getError()) {
                result.getError().printStackTrace();
            }
            assertFalse("The property should be fetched successfully", result.isError());
            assertEquals(propertyValue, result.getResult());
        });

        component.clearProperty(namespace, propertyName, result -> {
            if (null != result.getError()) {
                result.getError().printStackTrace();
            }
            assertFalse("The property should be cleared successfully", result.isError());
        });

        // retrieve the default for a non-existent property
        component.getProperty(namespace, propertyName, "defaultValue", result -> {
            if (null != result.getError()) {
                result.getError().printStackTrace();
            }
            assertFalse("The default property value should be returned", result.isError());
            assertEquals("defaultValue", result.getResult());
        });
    }
}
