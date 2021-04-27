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

package io.apiman.gateway.platforms.vertx3.logging;

import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.logging.IDelegateFactory;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class VertxLoggerDelegate implements IDelegateFactory {

    // For the Apiman logger system.
    @Override
    public IApimanLogger createLogger(String name) {
        return new VertxApimanLogger(LoggerFactory.getLogger(name));
    }

    @Override
    public IApimanLogger createLogger(Class<?> klazz) {
        return new VertxApimanLogger(LoggerFactory.getLogger(klazz));
    }

    static class VertxApimanLogger implements IApimanLogger {
        private final Logger logger;

        VertxApimanLogger(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void info(String message) {
            logger.info(message);
        }

        @Override
        public void info(String message, Object... args) {
            logger.info(message, args);
        }

        @Override
        public void warn(String message) {
            logger.warn(message);
        }

        @Override
        public void warn(String message, Object... args) {
            logger.warn(message, args);
        }

        @Override
        public void debug(String message) {
            logger.debug(message);
        }

        @Override
        public void debug(String message, Object... args) {
            logger.debug(message, args);
        }

        @Override
        public void trace(String message) {
            logger.trace(message);
        }

        @Override
        public void trace(String message, Object... args) {
            logger.trace(message, args);
        }

        @Override
        public void error(Throwable error) {
            logger.error(error);
        }

        @Override
        public void error(String message, Throwable error) {
            logger.error(message, error);
        }

        @Override
        public void error(Throwable error, String message, Object... args) {
            logger.error(message, error, args);
        }
    }

}
