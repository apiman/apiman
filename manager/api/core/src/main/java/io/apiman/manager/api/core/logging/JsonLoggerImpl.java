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
package io.apiman.manager.api.core.logging;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple JSON logger, see {@link #jsonify(String, LogLevel, Throwable)}. Aims to provide
 *
 * Jackson's pojo2json functionality is used on Throwables to create an easily parseable exception structure.
 *
 * Example:
 *
 * <pre>
 * <code>
 * {
 *     "@timestamp": 1430386948306,
 *     "message": "log message here",
 *     "throwable": {
 *         "message": "a terrible thing happened",
 *         "cause": {
 *             "cause": {
 *                 "cause": null,
 *                 "stackTrace": [
 *                     {
 *                         "methodName": "main",
 *                         "fileName": "Main.java",
 *                         "lineNumber": 208,
 *                         "className": "com.company.Main",
 *                         "nativeMethod": false
 *                     },
 *                     {
 *                         "methodName": "invoke0",
 *                         "fileName": "NativeMethodAccessorImpl.java",
 *                         "lineNumber": -2,
 *                         "className": "sun.reflect.NativeMethodAccessorImpl",
 *                         "nativeMethod": true
 *                     },
 *                     {
 *                         "methodName": "invoke",
 *                         "fileName": "NativeMethodAccessorImpl.java",
 *                         "lineNumber": 57,
 *                         "className": "sun.reflect.NativeMethodAccessorImpl",
 *                         "nativeMethod": false
 *                     },
 *                     ... SNIP
 *                 ],
 *                 "message": null,
 *                 "localizedMessage": null,
 *                 "suppressed": []
 *             },
 *             "stackTrace": [
 *                 {
 *                     "methodName": "main",
 *                     "fileName": "Main.java",
 *                     "lineNumber": 208,
 *                     "className": "com.company.Main",
 *                     "nativeMethod": false
 *                 },
 *                 {
 *                     "methodName": "invoke0",
 *                     "fileName": "NativeMethodAccessorImpl.java",
 *                     "lineNumber": -2,
 *                     "className": "sun.reflect.NativeMethodAccessorImpl",
 *                     "nativeMethod": true
 *                 },
 *                 {
 *                     "methodName": "invoke",
 *                     "fileName": "NativeMethodAccessorImpl.java",
 *                     "lineNumber": 57,
 *                     "className": "sun.reflect.NativeMethodAccessorImpl",
 *                     "nativeMethod": false
 *                 },
 *                 ... SNIP
 *             ],
 *             "message": "a terrible thing happened",
 *             "localizedMessage": "algo terrible ha ocurrido!",
 *             "suppressed": []
 *         }
 *     }
 * }
 * </code>
 * </pre>
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class JsonLoggerImpl implements IApimanDelegateLogger {
    /**
     * @author Marc Savy <msavy@redhat.com>
     *
     */
    private enum LogLevel {
        INFO, DEBUG, WARN, TRACE, ERROR
    }

    private static Time time = new DefaultTimeImpl();
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        //mapper.set TODO set pretty print?
    }

    private Logger delegatedLogger;
    private Class<?> klazz;

    /**
     * Instantiate a JsonLogger
     *
     * @param klazz the class instantiating logger
     */
    @Override
    public IApimanLogger createLogger(Class <?> klazz) {
        delegatedLogger = LogManager.getLogger(klazz);
        this.klazz = klazz;
        return this;
    }

    /**
     * Instantiate a JsonLogger
     *
     * @param name the logger name
     */
    @Override
    public IApimanLogger createLogger(String name) {
        delegatedLogger = LogManager.getLogger(name);
        return this;
    }

    /**
     * Set the time implementation.
     * Particularly useful for testing.
     *
     * @param timeImpl the time implementation.
     */
    public static void setTimeImpl(Time timeImpl) {
        time = timeImpl;
    }

    @Override
    public void info(String message) {
        delegatedLogger.info(jsonify(message, LogLevel.INFO));
    }

    @Override
    public void warn(String message) {
        delegatedLogger.warn(jsonify(message, LogLevel.WARN));
    }

    @Override
    public void debug(String message) {
        delegatedLogger.debug(jsonify(message, LogLevel.DEBUG));
    }

    @Override
    public void trace(String message) {
        delegatedLogger.trace(jsonify(message, LogLevel.TRACE));
    }

    @Override
    public void error(String message, Throwable error) {
        delegatedLogger.error(jsonify(message, LogLevel.ERROR, error));
    }

    @Override
    public void error(Throwable error) {
        delegatedLogger.error(jsonify(error.getMessage(), LogLevel.ERROR, error));
    }

    private String jsonify(String message, LogLevel level) {
        return jsonify(message, level, null);
    }

    @SuppressWarnings("nls")
    private String jsonify(String message, LogLevel level, Throwable t) {
        try { // TODO something more accurate for guessing SW length
            int traceLen = t == null ? 0 :  t.getStackTrace().length * 800;

            StringWriter sw = new StringWriter(100 + message.length() + traceLen);
            JsonGenerator generator = mapper.getFactory().createGenerator(sw);

            generator.writeStartObject();
            generator.writeStringField("@timestamp", time.currentTimeIso8601());
            generator.writeStringField("level", level.toString());
            generator.writeStringField("loggerName", klazz.getCanonicalName());
            generator.writeStringField("thread", Thread.currentThread().getName());
            generator.writeStringField("message", message);

            if (t != null) {
                generator.writeObjectFieldStart("throwable");
                  generator.writeStringField("message", t.getLocalizedMessage());
                  generator.writeObjectField("cause", t);
                generator.writeEndObject();
            }

            generator.writeEndObject();
            generator.close();

            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
