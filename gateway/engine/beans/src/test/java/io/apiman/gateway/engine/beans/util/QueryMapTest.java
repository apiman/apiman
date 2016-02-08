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

package io.apiman.gateway.engine.beans.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class QueryMapTest {

    @Test
    public void simpleQuery() {
        QueryMap qm = new QueryMap();
        qm.add("q", "search").add("Q", "search again").add("x", "otherthing");
        Assert.assertEquals("q=search&Q=search+again&x=otherthing", qm.toQueryString());
    }

    @Test
    public void complexQuery() {
        QueryMap qm = new QueryMap();
        qm.add("the query param", "the meaning of life: 42, according to Douglas Adams' Hitchiker's Guide to the Galaxy")
          .add("Q", "A & B & C & D * E @ F $ G % G ^ I & J ( K");

        Assert.assertEquals("the+query+param=the+meaning+of+life%3A+42%2C+according+to+Douglas+Adams%27+Hitchiker%27s+Guide+to+the+Galaxy&"
                + "Q=A+%26+B+%26+C+%26+D+*+E+%40+F+%24+G+%25+G+%5E+I+%26+J+%28+K", qm.toQueryString());
    }

}
