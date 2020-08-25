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

import io.apiman.common.es.util.AbstractEsComponent;
import io.apiman.common.es.util.EsConstants;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ISharedStateComponent;
import io.apiman.gateway.engine.es.beans.PrimitiveBean;
import io.apiman.gateway.engine.storage.util.BackingStoreUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import static io.apiman.gateway.engine.storage.util.BackingStoreUtil.JSON_MAPPER;

/**
 * An elasticsearch implementation of the shared state component.
 *
 * @author eric.wittmann@redhat.com
 */
public class EsSharedStateComponent extends AbstractEsComponent implements ISharedStateComponent {

    /**
     * Constructor.
     * @param config the configuration
     */
    public EsSharedStateComponent(Map<String, String> config) {
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
            GetResponse response = getClient().get(new GetRequest(getFullIndexName()).id(id), RequestOptions.DEFAULT);
            if (response.isExists()) {
                try {
                    T value;
                    if (defaultValue.getClass().isPrimitive() || defaultValue instanceof String) {
                        value = (T) readPrimitive(response);
                    } else {
                        String sourceAsString = response.getSourceAsString();
                        value = (T) JSON_MAPPER.readValue(sourceAsString, defaultValue.getClass());
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
        String source;
        try {
            if (value.getClass().isPrimitive() || value instanceof String) {
                PrimitiveBean pb = new PrimitiveBean();
                pb.setValue(String.valueOf(value));
                pb.setType(value.getClass().getName());
                source = JSON_MAPPER.writeValueAsString(pb);
            } else {
                source = JSON_MAPPER.writeValueAsString(value);
            }
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.<Void>create(e));
            return;
        }

        String id = getPropertyId(namespace, propertyName);
        String json = source;
        IndexRequest indexRequest = new IndexRequest(getFullIndexName()).source(json, XContentType.JSON).id(id);

        try {
            getClient().index(indexRequest, RequestOptions.DEFAULT);
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
        DeleteRequest deleteRequest = new DeleteRequest(getFullIndexName(), id);
        try {
            getClient().delete(deleteRequest, RequestOptions.DEFAULT);
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
        return Base64.encodeBase64String(qn.getBytes());
    }

    /**
     * Reads a stored primitive.
     * @param response
     */
    protected Object readPrimitive(GetResponse response) throws Exception {
        String sourceAsString = response.getSourceAsString();
        PrimitiveBean pb = JSON_MAPPER.readValue(sourceAsString,PrimitiveBean.class);
        String value = pb.getValue();
        Class<?> c = Class.forName(pb.getType());
        return BackingStoreUtil.readPrimitive(c, value);
    }

    /**
     * @see AbstractEsComponent#getDefaultIndexPrefix()
     */
    @Override
    protected String getDefaultIndexPrefix() {
        return EsConstants.GATEWAY_INDEX_NAME;
    }

    /**
     * @see AbstractEsComponent#getDefaultIndices()
     * @return default indices
     */
    @Override
    protected List<String> getDefaultIndices() {
        String[] indices = {EsConstants.INDEX_SHARED_STATE_PROPERTY};
        return Arrays.asList(indices);
    }

    /**
     * get index full name for shared state property
     * @return full index name
     */
    private String getFullIndexName() {
        return (getIndexPrefix() + EsConstants.INDEX_SHARED_STATE_PROPERTY).toLowerCase();
    }

}
