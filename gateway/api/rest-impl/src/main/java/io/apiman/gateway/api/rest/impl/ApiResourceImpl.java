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

import io.apiman.gateway.api.rest.IApiResource;
import io.apiman.gateway.api.rest.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Implementation of the API API :).
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiResourceImpl extends AbstractResourceImpl implements IApiResource {

    /**
     * Constructor.
     */
    public ApiResourceImpl() {
    }

    /**
     * @see IApiResource#publish(io.apiman.gateway.engine.beans.Api)
     */
    @Override
    public void publish(Api api) throws PublishingException, NotAuthorizedException {
        final Set<Throwable> errorHolder = new HashSet<>();
        final CountDownLatch latch = new CountDownLatch(1);
        // Publish api; latch until result returned and evaluated
        getEngine().getRegistry().publishApi(api, latchedResultHandler(latch, errorHolder));
        awaitOnLatch(latch, errorHolder);
    }

    /**
     * @see IApiResource#retire(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void retire(String organizationId, String apiId, String version) throws RegistrationException,
            NotAuthorizedException {
        final Set<Throwable> errorHolder = new HashSet<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Api api = new Api();
        api.setOrganizationId(organizationId);
        api.setApiId(apiId);
        api.setVersion(version);
        // Retire api; latch until result returned and evaluated
        getEngine().getRegistry().retireApi(api, latchedResultHandler(latch, errorHolder));
        awaitOnLatch(latch, errorHolder);
    }

    /**
     * @see IApiResource#getApiEndpoint(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiEndpoint getApiEndpoint(String organizationId, String apiId, String version)
            throws NotAuthorizedException {
        return getPlatform().getApiEndpoint(organizationId, apiId, version);
    }

    @Override
    public void retire(String organizationId, String apiId, String version, AsyncResponse response)
            throws RegistrationException, NotAuthorizedException {
        Api api = new Api();
        api.setOrganizationId(organizationId);
        api.setApiId(apiId);
        api.setVersion(version);
        getEngine().getRegistry().retireApi(api, handlerWithEmptyResult(response));
    }

    @Override
    public void getApiEndpoint(String organizationId, String apiId, String version, AsyncResponse response)
            throws NotAuthorizedException {
        ApiEndpoint apiEndpoint = getPlatform().getApiEndpoint(organizationId, apiId, version);
        response.resume(Response.ok(apiEndpoint).build());
    }


    @Override
    public void listApis(String organizationId, int page, int pageSize, AsyncResponse response)
            throws NotAuthorizedException {
        getEngine().getRegistry().listApis(organizationId, page, pageSize, handlerWithResult(response));
    }


    @Override
    public void listApiVersions(String organizationId, String apiId, int page, int pageSize, AsyncResponse response)
            throws NotAuthorizedException {
        getEngine().getRegistry().listApiVersions(organizationId, apiId, page, pageSize, handlerWithResult(response));
    }

    @Override
    public void getApiVersion(String organizationId, String apiId, String version, AsyncResponse response)
            throws NotAuthorizedException {
        getEngine().getRegistry().getApi(organizationId, apiId, version, result -> {
            if (result.isSuccess()) {
                Api api = result.getResult();
                if (api == null) {
                    response.resume(Response.status(Status.NOT_FOUND).build());
                } else {
                    response.resume(Response.ok(api).build());
                }
            } else {
                throwError(result.getError());
            }
        });
    }

}
