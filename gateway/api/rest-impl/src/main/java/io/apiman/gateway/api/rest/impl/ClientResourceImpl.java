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

package io.apiman.gateway.api.rest.impl;

import io.apiman.gateway.api.rest.contract.IClientResource;
import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.exceptions.AbstractEngineException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;


/**
 * Implementation of the Client API.
 *
 * @author eric.wittmann@redhat.com
 */
public class ClientResourceImpl extends AbstractResourceImpl implements IClientResource {

    /**
     * Constructor.
     */
    public ClientResourceImpl() {
    }

    /**
     * @see io.apiman.gateway.api.rest.contract.IClientResource#register(io.apiman.gateway.engine.beans.Client)
     */
    @Override
    public void register(Client client) throws RegistrationException, NotAuthorizedException {
        final Set<Throwable> errorHolder = new HashSet<>();
        final CountDownLatch latch = new CountDownLatch(1);
        getEngine().getRegistry().registerClient(client, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isError()) {
                    errorHolder.add(result.getError());
                }
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!errorHolder.isEmpty()) {
            Throwable error = errorHolder.iterator().next();
            if (error instanceof AbstractEngineException) {
                throw (AbstractEngineException) error;
            } else {
                throw new RuntimeException(error);
            }
        }
    }

    /**
     * @see io.apiman.gateway.api.rest.contract.IClientResource#unregister(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void unregister(String organizationId, String applicationId, String version)
            throws RegistrationException, NotAuthorizedException {
        final Set<Throwable> errorHolder = new HashSet<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Client application = new Client();
        application.setOrganizationId(organizationId);
        application.setClientId(applicationId);
        application.setVersion(version);
        getEngine().getRegistry().unregisterClient(application, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isError()) {
                    errorHolder.add(result.getError());
                }
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!errorHolder.isEmpty()) {
            Throwable error = errorHolder.iterator().next();
            if (error instanceof AbstractEngineException) {
                throw (AbstractEngineException) error;
            } else {
                throw new RuntimeException(error);
            }
        }
    }

}
