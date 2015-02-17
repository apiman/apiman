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
 * distributed under the License is distributed on an "AS I/me S" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.plugins.cors_policy;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import io.apiman.gateway.engine.IServiceConnection;
import io.apiman.gateway.engine.IServiceConnectionResponse;
import io.apiman.gateway.engine.IServiceConnector;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.HeaderHashMap;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.plugins.cors_policy.util.HttpHelper;

/**
 * CORS validator and connector.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class CorsConnector implements IServiceConnector {

    // Request related headers
    public static final String ORIGIN_KEY = "Origin"; //$NON-NLS-1$
    public static final String HOST_KEY = "Host"; //$NON-NLS-1$
    public static final String AC_REQUEST_METHOD_KEY = "Access-Control-Request-Method"; //$NON-NLS-1$
    public static final String AC_REQUEST_HEADERS_KEY = "Access-Control-Request-Headers"; //$NON-NLS-1$

    // Response related headers
    public static final String AC_ALLOW_ORIGIN_KEY = "Access-Control-Allow-Origin"; //$NON-NLS-1$
    public static final String AC_MAX_AGE_KEY = "Access-Control-Max-Age"; //$NON-NLS-1$
    public static final String AC_ALLOW_METHODS_KEY = "Access-Control-Allow-Methods"; //$NON-NLS-1$
    public static final String AC_EXPOSE_HEADERS_KEY = "Access-Control-Expose-Headers"; //$NON-NLS-1$
    public static final String AC_ALLOW_HEADERS_KEY = "Access-Control-Allow-Headers"; //$NON-NLS-1$
    public static final String AC_ALLOW_CREDENTIALS_KEY = "Access-Control-Allow-Credentials"; //$NON-NLS-1$

    private static final String CONTENT_TYPE = "Content-Type"; //$NON-NLS-1$

    // CORS conversation related fields
    private CorsConfigBean config;
    private ServiceRequest request;
    private Map<String, String> requestHeaders;
    private Map<String, String> responseHeaders = new HeaderHashMap();
    private boolean shortCircuit = false;
    private PolicyFailure failure = null;
    private IPolicyFailureFactoryComponent failureFactory;

    /**
     * {@link CorsConnector} determines whether
     * 
     * @author Marc Savy <msavy@redhat.com>
     */
    public CorsConnector(ServiceRequest request, CorsConfigBean config, IPolicyFailureFactoryComponent failureFactory) {
        this.request = request;
        this.config = config;
        this.failureFactory = failureFactory;

        requestHeaders = request.getHeaders();

        doCors();
    }

    @Override
    public IServiceConnection connect(ServiceRequest request,
            IAsyncResultHandler<IServiceConnectionResponse> handler) throws ConnectorException {

        return new ShortcircuitServiceConnection(handler);
    }

    /**
     * @return Whether it's a preflight request and should be short-circuited
     */
    public boolean isShortcircuit() {
        return shortCircuit;
    }

    /**
     * @return Whether CORS validation failed and the failure flag is set
     */
    public boolean isFailure() {
        return failure != null;
    }

    /**
     * @return The failure if failure occurred, else null
     */
    public PolicyFailure getFailure() {
        return failure;
    }

    /**
     * @return Calculated CORs response headers
     */
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Is the request related to CORS? Helps avoid unnecessary object creation.
     * 
     * @param request
     * @return true if CORS is a request
     */
    public static boolean candidateCorsRequest(ServiceRequest request) {
        return request.getHeaders().get(ORIGIN_KEY) != null;
    }

    private void doCors() {
        String origin = requestHeaders.get(ORIGIN_KEY);
        String host = requestHeaders.get(HOST_KEY);
        String[] acRequestHeaders = split(requestHeaders.get(AC_REQUEST_HEADERS_KEY));

        // Is the request an allowed origin? If so, echo back the allowed origin.
        appendMandatory(AC_ALLOW_ORIGIN_KEY, origin,
                "", config.isAllowedOrigin(origin, host), Messages.getString("CorsConnector.origin_not_permitted")); //$NON-NLS-1$ //$NON-NLS-2$

        // Do nothing on failure
        appendOptional(AC_ALLOW_CREDENTIALS_KEY, Boolean.TRUE.toString(), config.isAllowCredentials(), null);

        // Do nothing if there are no exposed headers.
        appendOptional(AC_EXPOSE_HEADERS_KEY, join(config.getExposeHeaders()), !config.getExposeHeaders()
                .isEmpty(), null);

        if (!isSimpleRequest(acRequestHeaders)) { // Should be a preflight request
            if (request.getType().equals(HttpHelper.OPTIONS)) {
                String[] acRequestMethods = split(requestHeaders.get(AC_REQUEST_METHOD_KEY));

                // Append allowed methods even if we return an error.
                String allowMethods = join(config.getAllowMethods());

                appendMandatory(AC_ALLOW_METHODS_KEY, allowMethods, allowMethods,
                        config.isAllowedMethod(acRequestMethods), Messages.getString("CorsConnector.requested_method_not_allowed")); //$NON-NLS-1$

                // Append allowed headers even if we return an error.
                if (!config.getAllowHeaders().isEmpty() && acRequestHeaders != null) {
                    appendOptional(AC_ALLOW_HEADERS_KEY, join(config.getAllowHeaders()),
                            config.isAllowedHeader(acRequestHeaders), Messages.getString("CorsConnector.requested_header_not_allowed")); //$NON-NLS-1$
                }

                appendOptional(AC_MAX_AGE_KEY, config.getMaxAge().toString(), config.getMaxAge() != null,
                        null);

                // Only short-circuit if it's a *successful* preflight request
                if (!isFailure()) {
                    shortCircuit = true;
                }
            } else {
                doFailure(Messages.getString("CorsConnector.invalid_preflight_request")); //$NON-NLS-1$
            }
        }
    }

    private void doFailure(String string) {
        failure = failureFactory.createFailure(PolicyFailureType.Authorization, 400, "CORS: " + string); //$NON-NLS-1$
        failure.setHeaders(responseHeaders);
        failure.setResponseCode(400);
    }

    /**
     * @return if the request is simple, as defined by CORS spec
     */
    private boolean isSimpleRequest(String... headers) {
        String contentType = requestHeaders.get(CONTENT_TYPE);
        String reqType = request.getType();

        if (HttpHelper.isSimpleMethod(reqType)) {
            if (reqType == HttpHelper.POST) {
                return HttpHelper.isSimpleContentType(contentType)
                        && (headers == null || HttpHelper.isSimpleHeader(headers));
            } else {
                return headers == null || HttpHelper.isSimpleHeader(headers);
            }
        }

        return false;
    }

    private void appendMandatory(String name, String value, String defaultValue, boolean predicate,
            String failureMessage) {

        if (value == null || !predicate) {
            value = defaultValue;

            if (!predicate && config.isErrorOnCorsFailure()) {
                doFailure(failureMessage);
            }
        }

        responseHeaders.put(name, value);
    }

    private void appendOptional(String name, String value, boolean predicate, String failureMessage) {
        if (value != null && predicate) {
            responseHeaders.put(name, value);
        } else {
            if (config.isErrorOnCorsFailure() && failureMessage != null) {
                doFailure(failureMessage);
            }
        }
    }

    private String join(Set<String> set) {
        StringBuilder joined = new StringBuilder();
        String delim = ""; //$NON-NLS-1$

        for (String s : set) {
            joined.append(delim);
            joined.append(s);
            delim = ", "; //$NON-NLS-1$
        }

        return joined.toString();
    }

    private String[] split(String input) {
        if (input == null)
            return null;
        return StringUtils.stripAll(input.split(",")); //$NON-NLS-1$
    }

    /**
     * A connection consisting predominantly dummy methods as we're not contacting a real service.
     * 
     * @author Marc Savy <msavy@redhat.com>
     */
    class ShortcircuitServiceConnection implements IServiceConnection, IServiceConnectionResponse {
        private boolean finished = false;
        private IAsyncHandler<Void> endHandler;
        private IAsyncResultHandler<IServiceConnectionResponse> responseHandler;
        private ServiceResponse response;

        public ShortcircuitServiceConnection(IAsyncResultHandler<IServiceConnectionResponse> handler) {
            responseHandler = handler;

            response = new ServiceResponse();
            response.setCode(200);
            response.setHeaders(responseHeaders);
        }

        @Override
        public void abort() {
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public void write(IApimanBuffer chunk) {
        }

        @Override
        public void end() {
            responseHandler.handle(AsyncResultImpl.<IServiceConnectionResponse> create(this));
        }

        // We're now okay to do our baked response.
        @Override
        public void transmit() {
            endHandler.handle((Void) null);
            finished = true;
        }

        @Override
        public void bodyHandler(IAsyncHandler<IApimanBuffer> bodyHandler) {
        }

        @Override
        public void endHandler(IAsyncHandler<Void> endHandler) {
            this.endHandler = endHandler;
        }

        @Override
        public ServiceResponse getHead() {
            return response;
        }
    }
}
