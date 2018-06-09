/*
 * Copyright 2018 JBoss Inc
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

package io.apiman.gateway.engine.impl;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@SuppressWarnings("nls")
public class AbstractConnectorConfigTest {

    static final Set<String> REQUEST = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    static final Set<String> RESPONSE = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    static {
        REQUEST.add("Transfer-Encoding");
        REQUEST.add("X-API-Key");
        REQUEST.add("Host");

        RESPONSE.add("Transfer-Encoding");
        RESPONSE.add("Connection");
    }

    @Test
    public void shouldContainInitialHeaders() {
        AbstractConnectorConfig connector = new AbstractConnectorConfig(REQUEST, RESPONSE) {};
        Assert.assertTrue(connector.getSuppressedResponseHeaders().containsAll(RESPONSE));
    }

    @Test
    public void shouldEvaluateKeysCaseInsensitively() {
        AbstractConnectorConfig connector = new AbstractConnectorConfig(REQUEST, RESPONSE) {};
        Assert.assertTrue("Must compare case insensitively", connector.getSuppressedRequestHeaders().contains("x-api-key"));
    }

    @Test
    public void addSuppressedRequestHeader() {
        AbstractConnectorConfig connector = new AbstractConnectorConfig(REQUEST, RESPONSE) {};
        connector.suppressRequestHeader("Seychelles");
        Assert.assertTrue("Must contain suppressed header added by user", connector.getSuppressedRequestHeaders().contains("seychelles"));
    }

    @Test
    public void permitRequestHeader() {
        AbstractConnectorConfig connector = new AbstractConnectorConfig(REQUEST, RESPONSE) {};
        connector.permitRequestHeader("Host");
        Assert.assertFalse("Must unsuppress header", connector.getSuppressedRequestHeaders().contains("Host"));
    }

    @Test
    public void addSuppressedResponseHeader() {
        AbstractConnectorConfig connector = new AbstractConnectorConfig(REQUEST, RESPONSE) {};
        connector.suppressResponseHeader("Aldabra");
        Assert.assertTrue("Must contain suppressed header added by user", connector.getSuppressedResponseHeaders().contains("Aldabra"));
    }

    @Test
    public void permitResponseHeader() {
        AbstractConnectorConfig connector = new AbstractConnectorConfig(REQUEST, RESPONSE) {};
        connector.permitResponseHeader("Host");
        Assert.assertFalse("Must unsuppress header", connector.getSuppressedResponseHeaders().contains("Host"));
    }

}
