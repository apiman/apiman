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
package io.apiman.manager.api.logging;

import io.apiman.manager.api.core.logging.IApimanLogger;
import io.apiman.manager.api.core.logging.LoggerFactory;
import io.apiman.manager.api.core.logging.Time;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link LoggerFactory}.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class JsonLoggerFactoryTest {

    private PrintStream oldOut = System.out; // Keep old output, put it back afterwards;
    private ByteArrayOutputStream outputStream;

    @Before
    public void before() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @After
    public void after() {
        System.setOut(oldOut);
    }

    @Test
    public void shouldCreateExceptionalJsonMessage() {
        IApimanLogger logger = LoggerFactory.createLogger("myLogger");
        logger.setTimeImpl(new Time() {

            @Override
            public long currentTimeMillis() {
                return 1234;
            }
        });

        logger.debug("This is a debug message", new RuntimeException("boom"));

        int indexOf = StringUtils.trim(outputStream.toString()).indexOf("{\"@timestamp\":1234,\"message\":\"This is a debug message\",\"throwable\":{\"message\":\"boom\",\"cause\":{\"cause\":null,\"stackTrace\":[{\"methodName\":\"shouldCreateExceptionalJsonMessage\"");

        Assert.assertTrue("Exception trace was expected", indexOf>=0);
    }
}
