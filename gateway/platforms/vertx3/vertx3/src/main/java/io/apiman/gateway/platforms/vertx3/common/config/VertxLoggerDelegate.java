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

package io.apiman.gateway.platforms.vertx3.common.config;

import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.logging.IDelegateFactory;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class VertxLoggerDelegate implements IDelegateFactory {

    @Override
    public IApimanLogger createLogger(String name) {
        return new VertxApimanLogger(LoggerFactory.getLogger(name));
    }

    @Override
    public IApimanLogger createLogger(Class<?> klazz) {
        return new VertxApimanLogger(LoggerFactory.getLogger(klazz));
    }

    class VertxApimanLogger implements IApimanLogger {
        private Logger logger;

        public VertxApimanLogger(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void info(String message) {
            logger.info(message);
        }

        @Override
        public void warn(String message) {
            logger.warn(message);
        }

        @Override
        public void debug(String message) {
            logger.debug(message);
        }

        @Override
        public void trace(String message) {
            logger.trace(message);
        }

        @Override
        public void error(Throwable error) {
            logger.error(error);
        }

        @Override
        public void error(String message, Throwable error) {
            logger.error(message, error);
        }
    }

}
