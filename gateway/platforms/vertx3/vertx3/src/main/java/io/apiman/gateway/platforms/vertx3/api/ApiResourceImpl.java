/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.gateway.platforms.vertx3.api;

import io.apiman.common.util.SimpleStringUtils;
import io.apiman.gateway.api.rest.contract.IApiResource;
import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;

import java.net.URI;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class ApiResourceImpl extends AbstractResource implements IApiResource {

    private VertxEngineConfig apimanConfig;
    private IRegistry registry;

    public ApiResourceImpl(VertxEngineConfig apimanConfig, IEngine engine) {
        this.apimanConfig = apimanConfig;
        this.registry = engine.getRegistry();
    }

    @Override
    public void publish(Api api) throws PublishingException, NotAuthorizedException {
        registry.publishApi(api, (IAsyncResultHandler<Void>) result -> {
            if (result.isError()) {
                throwError(result.getError());
            }
        });
    }

    @Override
    public void retire(String organizationId, String apiId, String version) throws RegistrationException, NotAuthorizedException {
        Api api = new Api();
        api.setOrganizationId(organizationId);
        api.setApiId(apiId);
        api.setVersion(version);
        registry.retireApi(api, (IAsyncResultHandler<Void>) result -> {
            if (result.isError()) {
                throwError(result.getError());
            }
        });
    }

    @Override
    @SuppressWarnings("nls")
    public ApiEndpoint getApiEndpoint(String organizationId, String apiId, String version)
            throws NotAuthorizedException {
        String scheme = apimanConfig.preferSecure() ? "https" : "http";
        int port = apimanConfig.getPort(scheme);
        String host = "localhost"; // TODO host from request context
        String path = "";
        // If endpoint was manually specified
        if (apimanConfig.getPublicEndpoint() != null) {
           URI publicEndpoint = URI.create(apimanConfig.getPublicEndpoint());

           if (publicEndpoint.getPort() != -1) {
               port = publicEndpoint.getPort();
           }
           if (publicEndpoint.getScheme() != null && !publicEndpoint.getScheme().isEmpty()) {
               scheme = publicEndpoint.getScheme();
           }
           if (publicEndpoint.getPath() != null && !publicEndpoint.getPath().isEmpty()) {
               path = publicEndpoint.getPath();
           }
           if (publicEndpoint.getHost() != null && !publicEndpoint.getHost().isEmpty()) {
               host = publicEndpoint.getHost();
           }
        }

        String endpoint = scheme + "://" + host;
        if (port != 443 && port != 80)
            endpoint += ":" + port + "/";
        endpoint += path;
        endpoint += SimpleStringUtils.join("/", organizationId, apiId, version);

        ApiEndpoint endpointObj = new ApiEndpoint();
        endpointObj.setEndpoint(endpoint);
        return endpointObj;
    }

    @Override
    public void retire(String organizationId, String apiId, String version, AsyncResponse response)
            throws RegistrationException, NotAuthorizedException {
        Api api = new Api();
        api.setOrganizationId(organizationId);
        api.setApiId(apiId);
        api.setVersion(version);
        registry.retireApi(api, handlerWithEmptyResult(response));
    }

    @Override
    public void getApiEndpoint(String organizationId, String apiId, String version, AsyncResponse response) throws NotAuthorizedException {
        ApiEndpoint apiEndpoint = getApiEndpoint(organizationId, apiId, version);
        response.resume(Response.ok(apiEndpoint).build());
    }

    @Override
    public void listApis(String organizationId, int page, int pageSize, AsyncResponse response) throws NotAuthorizedException {
        registry.listApis(organizationId, page, pageSize, handlerWithResult(response));
    }

    @Override
    public void listApiVersions(String organizationId, String apiId, int page, int pageSize, AsyncResponse response) throws NotAuthorizedException {
        registry.listApiVersions(organizationId, apiId, page, pageSize, handlerWithResult(response));
    }

    @Override
    public void getApiVersion(String organizationId, String apiId, String version, AsyncResponse response) throws NotAuthorizedException {
        registry.getApi(organizationId, apiId, version, result -> {
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
