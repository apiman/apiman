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
package io.apiman.common.logging.slf4j;

import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.logging.IDelegateFactory;
import io.apiman.common.logging.annotations.ApimanLoggerFactory;

import org.slf4j.LoggerFactory;

/**
 * SLF4J logger factory.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@ApimanLoggerFactory(name = "slf4j")
public class Slf4jLoggerFactory implements IDelegateFactory {

    @Override
    public IApimanLogger createLogger(String name) {
        return new Slf4jLoggerImpl(LoggerFactory.getLogger(name));
    }

    @Override
    public IApimanLogger createLogger(Class<?> klazz) {
        return new Slf4jLoggerImpl(LoggerFactory.getLogger(klazz));
    }
}
