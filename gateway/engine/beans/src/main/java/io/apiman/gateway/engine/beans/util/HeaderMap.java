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

import java.io.Serializable;

/**
 * A map of headers to associated values. It is possible to
 * have multiple values for a given key.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class HeaderMap extends CaseInsensitiveStringMultiMap implements Serializable {
    private static final long serialVersionUID = 5520378999006587108L;

    /**
     * Construct a HeaderMap with default capacity.
     */
    public HeaderMap() {
        super();
    }

    /**
     * Construct a HeaderMap
     *
     * @param sizeHint the size hint
     */
    public HeaderMap(int sizeHint) {
        super(sizeHint);
    }
}
