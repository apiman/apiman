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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Simple JSON logger, see {@link #jsonify(String, Throwable)}. Aims to provide
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
    private Time time = new Time() {

        @Override
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    };

    ObjectMapper mapper = new ObjectMapper();

    {
        //mapper.set TODO set pretty print?
    }

    private Logger delegatedLogger;

    /**
     * Instantiate a JsonLogger
     *
     * @param klazz the class instantiating logger
     */
    @Override
    public IApimanLogger createLogger(Class <?> klazz) {
        delegatedLogger = LogManager.getLogger(klazz);
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

    private String jsonify(String message) {
        return jsonify(message, null);
    }

    @SuppressWarnings("nls")
    private String jsonify(String message, Throwable t) {
        try { // TODO something more accurate for guessing SW length
            int traceLen = t == null ? 0 :  t.getStackTrace().length * 800;

            StringWriter sw = new StringWriter(100 + message.length() + traceLen);
            JsonGenerator generator = mapper.getJsonFactory().createJsonGenerator(sw);

            generator.writeStartObject();
            generator.writeNumberField("@timestamp", time.currentTimeMillis());
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

    @Override
    public void info(String message) {
        delegatedLogger.info(jsonify(message));
    }

    @Override
    public void info(String message, Throwable throwable) {
        delegatedLogger.info(jsonify(message, throwable));
    }

    @Override
    public void debug(String message) {
        delegatedLogger.debug(jsonify(message));
    }

    @Override
    public void debug(String message, Throwable throwable) {
        delegatedLogger.debug(jsonify(message, throwable));
    }

    @Override
    public void trace(String message) {
        delegatedLogger.trace(jsonify(message));
    }

    @Override
    public void trace(String message, Throwable throwable) {
        delegatedLogger.trace(jsonify(message, throwable));
    }

    @Override
    public void setTimeImpl(Time timeImpl) {
        this.time = timeImpl;
    }
}
