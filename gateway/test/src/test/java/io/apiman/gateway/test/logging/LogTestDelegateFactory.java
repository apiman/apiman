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

package io.apiman.gateway.test.logging;

import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.logging.IDelegateFactory;
import io.apiman.common.logging.annotations.ApimanLoggerFactory;

import java.util.Map;

@ApimanLoggerFactory(name = "test-logger")
public class LogTestDelegateFactory implements IDelegateFactory {
    @Override
    public IApimanLogger createLogger(String name) {
        return new TestLogger(name);
    }

    @Override
    public IApimanLogger createLogger(Class<?> klazz) {
        return new TestLogger(klazz);
    }

}
