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
package io.apiman.gateway.platforms.vertx3.common.verticles;

import io.apiman.common.util.JsonUtil;

import java.util.Collection;

import com.fasterxml.jackson.databind.type.TypeFactory;
import io.vertx.core.json.DecodeException;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class Json extends io.vertx.core.json.Json {
    public static <C extends Collection<? super T>, T> C decodeValue(String str, Class<C> collectionClazz, Class<T> targetClazz) throws DecodeException {
        try {
            return JsonUtil.getObjectMapper().readValue(str, TypeFactory.defaultInstance().constructCollectionType(collectionClazz, targetClazz));
        }
        catch (Exception e) {
            throw new DecodeException("Failed to decode:" + e.getMessage()); //$NON-NLS-1$
        }
    }

}
