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

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ISharedStateComponent;
import io.apiman.gateway.engine.es.beans.PrimitiveBean;

import java.util.Map;

import javax.xml.namespace.QName;

import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Base64;
import org.elasticsearch.common.xcontent.XContentType;

/**
 * An elasticsearch implementation of the shared state component.
 *
 * @author eric.wittmann@redhat.com
 */
public class ESSharedStateComponent implements ISharedStateComponent {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private Map<String, String> config;
    private Client esClient;

    /**
     * Constructor.
     * @param config the configuration
     */
    public ESSharedStateComponent(Map<String, String> config) {
        this.config = config;
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
        getClient().prepareGet(ESConstants.INDEX_NAME, "sharedStateProperty", id) //$NON-NLS-1$
            .execute(new ActionListener<GetResponse>() {
                @SuppressWarnings("unchecked")
                @Override
                public void onResponse(GetResponse response) {
                    if (response.isExists()) {
                        try {
                            String source = response.getSourceAsString();
                            T value = null;
                            if (defaultValue.getClass().isPrimitive() || defaultValue instanceof String) {
                                value = (T) readPrimitive(source);
                            } else {
                                value = mapper.reader(defaultValue.getClass()).readValue(source);
                            }
                            handler.handle(AsyncResultImpl.create(value));
                        } catch (Exception e) {
                            handler.handle(AsyncResultImpl.<T>create(e));
                        }
                    } else {
                        handler.handle(AsyncResultImpl.create(defaultValue));
                    }
                }
                @Override
                public void onFailure(Throwable e) {
                    handler.handle(AsyncResultImpl.<T>create(e));
                }
            });
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
        getClient().prepareIndex(ESConstants.INDEX_NAME, "sharedStateProperty", id) //$NON-NLS-1$
            .setSource(source)
            .setContentType(XContentType.JSON)
            .execute(new ActionListener<IndexResponse>() {
                @Override
                public void onResponse(IndexResponse response) {
                    handler.handle(AsyncResultImpl.create((Void) null));
                }
                @Override
                public void onFailure(Throwable e) {
                    handler.handle(AsyncResultImpl.<Void>create(e));
                }
            });
    }

    /**
     * @see io.apiman.gateway.engine.components.ISharedStateComponent#clearProperty(java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public <T> void clearProperty(final String namespace, final String propertyName, final IAsyncResultHandler<Void> handler) {
        String id = getPropertyId(namespace, propertyName);
        getClient().prepareDelete(ESConstants.INDEX_NAME, "sharedStateProperty", id) //$NON-NLS-1$
            .execute(new ActionListener<DeleteResponse>() {
                @Override
                public void onResponse(DeleteResponse response) {
                    handler.handle(AsyncResultImpl.create((Void) null));
                }
                @Override
                public void onFailure(Throwable e) {
                    handler.handle(AsyncResultImpl.<Void>create(e));
                }
            });
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
     * @param source
     */
    protected Object readPrimitive(String source) throws Exception {
        PrimitiveBean pb = mapper.reader(PrimitiveBean.class).readValue(source);
        String value = pb.getValue();
        Class<?> c = Class.forName(pb.getType());
        if (c == String.class) {
            return value;
        } else if (c == Long.class) {
            return Long.parseLong(source);
        } else if (c == Integer.class) {
            return Integer.parseInt(source);
        } else if (c == Double.class) {
            return Double.parseDouble(source);
        } else if (c == Boolean.class) {
            return Boolean.parseBoolean(source);
        } else if (c == Byte.class) {
            return Byte.parseByte(source);
        } else if (c == Short.class) {
            return Short.parseShort(source);
        } else if (c == Float.class) {
            return Float.parseFloat(source);
        } else {
            throw new Exception("Unsupported primitive: " + c); //$NON-NLS-1$
        }
    }

    /**
     * @return the esClient
     */
    public synchronized Client getClient() {
        if (esClient == null) {
            esClient = ESClientFactory.createClient(config);
        }
        return esClient;
    }
    
}
