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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A map of query parameters to associated values. It is possible to
 * have multiple values for a given key.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonSerialize(using = MultiMapSerializer.class)
@JsonDeserialize(using = MultiMapDeserializer.class)
public class QueryMap extends CaseInsensitiveStringMultiMap implements Serializable {
    private static final long serialVersionUID = -3539301043663183648L;

    /**
     *  Construct map with default capacity.
     */
    public QueryMap() {
        super();
    }

    /**
     * Construct a QueryMap.
     *
     * @param sizeHint the size hint
     */
    public QueryMap(int sizeHint) {
        super(sizeHint);
    }

    @Override
    public String toString() {
        return toQueryString();
    }

    @SuppressWarnings("nls")
    public String toQueryString() { // TODO optimise
        List<Entry<String, String>> elems = getEntries();
        Collections.reverse(elems);
        return elems.stream()
                .map(pair -> URLEnc(pair.getKey()) + "=" + URLEnc(pair.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String URLEnc(String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            System.err.println("Unable to URLEncode" + str); //$NON-NLS-1$
            return str;
        }
    }
}
