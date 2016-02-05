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
package io.apiman.gateway.engine.es;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ISharedStateComponent;
import io.apiman.gateway.engine.es.beans.PrimitiveBean;
import io.searchbox.client.JestResult;
import io.searchbox.core.Delete;
import io.searchbox.core.Get;
import io.searchbox.core.Index;

import java.util.Map;

import javax.xml.namespace.QName;

import org.elasticsearch.common.Base64;

/**
 * An elasticsearch implementation of the shared state component.
 *
 * @author eric.wittmann@redhat.com
 */
public class ESSharedStateComponent extends AbstractESComponent implements ISharedStateComponent {

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Constructor.
     * @param config the configuration
     */
    public ESSharedStateComponent(Map<String, String> config) {
        super(config);
    }

    /**
     * @see io.apiman.gateway.engine.components.ISharedStateComponent#getProperty(java.lang.String, java.lang.String, java.lang.Object, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public <T> void getProperty(final String namespace, final String propertyName, final T defaultValue,
            final IAsyncResultHandler<T> handler) {
        if (defaultValue == null) {
            handler.handle(AsyncResultImpl.<T>create(new Exception("Null defaultValue is not allowed."))); //$NON-NLS-1$
            return;
        }
        String id = getPropertyId(namespace, propertyName);

        try {
            Get get = new Get.Builder(getIndexName(), id).type("sharedStateProperty").build(); //$NON-NLS-1$
            JestResult result = getClient().execute(get);
            if (result.isSucceeded()) {
                try {
                    T value = null;
                    if (defaultValue.getClass().isPrimitive() || defaultValue instanceof String) {
                        value = (T) readPrimitive(result);
                    } else {
                        value = (T) result.getSourceAsObject(defaultValue.getClass());
                    }
                    handler.handle(AsyncResultImpl.create(value));
                } catch (Exception e) {
                    handler.handle(AsyncResultImpl.<T>create(e));
                }
            } else {
                handler.handle(AsyncResultImpl.create(defaultValue));
            }
        } catch (Throwable e) {
            handler.handle(AsyncResultImpl.<T>create(e));
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.ISharedStateComponent#setProperty(java.lang.String, java.lang.String, java.lang.Object, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public <T> void setProperty(final String namespace, final String propertyName, final T value, final IAsyncResultHandler<Void> handler) {
        if (value == null) {
            handler.handle(AsyncResultImpl.<Void>create(new Exception("Null value is not allowed."))); //$NON-NLS-1$
            return;
        }
        String source = null;
        try {
            if (value.getClass().isPrimitive() || value instanceof String) {
                PrimitiveBean pb = new PrimitiveBean();
                pb.setValue(String.valueOf(value));
                pb.setType(value.getClass().getName());
                source = mapper.writeValueAsString(pb);
            } else {
                source = mapper.writeValueAsString(value);
            }
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.<Void>create(e));
            return;
        }

        String id = getPropertyId(namespace, propertyName);
        String json = source;
        Index index = new Index.Builder(json).refresh(false).index(getIndexName())
                .type("sharedStateProperty").id(id).build(); //$NON-NLS-1$
        try {
            getClient().execute(index);
            handler.handle(AsyncResultImpl.create((Void) null));
        } catch (Throwable e) {
            handler.handle(AsyncResultImpl.<Void>create(e));
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.ISharedStateComponent#clearProperty(java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public <T> void clearProperty(final String namespace, final String propertyName, final IAsyncResultHandler<Void> handler) {
        String id = getPropertyId(namespace, propertyName);

        Delete delete = new Delete.Builder(id).index(getIndexName()).type("sharedStateProperty").build(); //$NON-NLS-1$
        try {
            getClient().execute(delete);
            handler.handle(AsyncResultImpl.create((Void) null));
        } catch (Throwable e) {
            handler.handle(AsyncResultImpl.<Void>create(e));
        }
    }

    /**
     * @param namespace
     * @param propertyName
     */
    private String getPropertyId(String namespace, String propertyName) {
        String qn = new QName(namespace, propertyName).toString();
        return Base64.encodeBytes(qn.getBytes());
    }

    /**
     * Reads a stored primitive.
     * @param result
     */
    protected Object readPrimitive(JestResult result) throws Exception {
        PrimitiveBean pb = result.getSourceAsObject(PrimitiveBean.class);
        String value = pb.getValue();
        Class<?> c = Class.forName(pb.getType());
        if (c == String.class) {
            return value;
        } else if (c == Long.class) {
            return Long.parseLong(value);
        } else if (c == Integer.class) {
            return Integer.parseInt(value);
        } else if (c == Double.class) {
            return Double.parseDouble(value);
        } else if (c == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (c == Byte.class) {
            return Byte.parseByte(value);
        } else if (c == Short.class) {
            return Short.parseShort(value);
        } else if (c == Float.class) {
            return Float.parseFloat(value);
        } else {
            throw new Exception("Unsupported primitive: " + c); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.gateway.engine.es.AbstractESComponent#getIndexName()
     */
    @Override
    protected String getIndexName() {
        return ESConstants.GATEWAY_INDEX_NAME;
    }

}
