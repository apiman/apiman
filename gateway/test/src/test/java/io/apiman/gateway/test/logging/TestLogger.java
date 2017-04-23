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
import io.apiman.gateway.engine.beans.util.CaseInsensitiveStringMultiMap;

@SuppressWarnings("nls")
public class TestLogger implements IApimanLogger {

    private CaseInsensitiveStringMultiMap mm;
    private String name;

    public TestLogger(String name) {
        this.name = name;
    }

    public TestLogger(Class<?> klazz) {
        this.name = klazz.getName();
    }

    public void setHeaders(CaseInsensitiveStringMultiMap mm) {
        this.mm = mm;
    }

    @Override
    public void info(String message) {
        mm.add("info", String.format("%s: %s", name, message));
    }

    @Override
    public void warn(String message) {
        mm.add("warn", String.format("%s: %s", name, message));
    }

    @Override
    public void debug(String message) {
        mm.add("debug", String.format("%s: %s", name, message));
    }

    @Override
    public void trace(String message) {
        mm.add("trace", String.format("%s: %s", name, message));
    }

    @Override
    public void error(Throwable error) {
        mm.add("error", String.format("%s: %s", name, error.getMessage()));
    }

    @Override
    public void error(String message, Throwable error) {
        mm.add("error_with_message", String.format("%s: %s - %s", name, message, error.getMessage()));
    }

    @Override
    public void info(String message, Object... args) {

    }

    @Override
    public void warn(String message, Object... args) {

    }

    @Override
    public void debug(String message, Object... args) {

    }

    @Override
    public void trace(String message, Object... args) {

    }

    @Override
    public void error(Throwable error, String message, Object... args) {

    }
}
