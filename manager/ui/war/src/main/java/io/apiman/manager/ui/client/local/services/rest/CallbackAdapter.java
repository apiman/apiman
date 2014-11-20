/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.manager.ui.client.local.services.rest;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;

import com.google.gwt.http.client.Request;

/**
 * Adapts an invoker callback into both a remote and error callback for errai.
 *
 * @author eric.wittmann@redhat.com
 */
public class CallbackAdapter<T> implements RemoteCallback<T>, RestErrorCallback {
    
    private IRestInvokerCallback<T> invokerCallback;
    
    /**
     * Constructor.
     * @param invokerCallback
     */
    public CallbackAdapter(IRestInvokerCallback<T> invokerCallback) {
        this.invokerCallback = invokerCallback;
    }
    
    /**
     * @see org.jboss.errai.common.client.api.RemoteCallback#callback(java.lang.Object)
     */
    @Override
    public void callback(T response) {
        this.invokerCallback.onSuccess(response);
    }
    
    /**
     * @see org.jboss.errai.common.client.api.ErrorCallback#error(java.lang.Object, java.lang.Throwable)
     */
    @Override
    public boolean error(Request message, Throwable throwable) {
        this.invokerCallback.onError(throwable);
        return false;
    }

}
