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

import io.apiman.gateway.engine.IApiConnection;
import io.apiman.gateway.engine.IApiConnectionResponse;
import io.apiman.gateway.engine.IApiConnector;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.plugins.cors_policy.util.HttpHelper;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * CORS validator and connector. Implements http://www.w3.org/TR/2014/REC-cors-20140116/.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class CorsConnector implements IApiConnector {

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

    public static final String CONTENT_TYPE = "Content-Type"; //$NON-NLS-1$

    // CORS conversation related fields
    private CorsConfigBean config;
    private ApiRequest request;
    private HeaderMap requestHeaders;
    private HeaderMap responseHeaders = new HeaderMap();
    private boolean shortCircuit = false;
    private PolicyFailure failure = null;
    private IPolicyFailureFactoryComponent failureFactory;

    /**
     * @param request the request
     * @param config the provided configuration
     * @param failureFactory the failure factory
     */
    public CorsConnector(ApiRequest request, CorsConfigBean config, IPolicyFailureFactoryComponent failureFactory) {
        this.request = request;
        this.config = config;
        this.failureFactory = failureFactory;

        requestHeaders = request.getHeaders();

        doCors();
    }

    @Override
    public IApiConnection connect(ApiRequest request,
            IAsyncResultHandler<IApiConnectionResponse> handler) throws ConnectorException {

        return new ShortcircuitApiConnection(handler);
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
    public HeaderMap getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Is the request related to CORS? Helps avoid unnecessary object creation.
     *
     * @param request the request
     * @return true if CORS is a request
     */
    public static boolean candidateCorsRequest(ApiRequest request) {
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

        if (!isSimpleRequest(acRequestHeaders)) { // Should be a preflight request or real request
            if (request.getType().equals(HttpHelper.OPTIONS) &&
                    requestHeaders.get(AC_REQUEST_METHOD_KEY) != null) {
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
            if (reqType.equals(HttpHelper.POST)) {
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
        String valToSet = value;
        if (value == null || !predicate) {
            valToSet = defaultValue;

            if (!predicate && config.isErrorOnCorsFailure()) {
                doFailure(failureMessage);
            }
        }

        responseHeaders.put(name, valToSet);
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
     * A connection consisting predominantly dummy methods as we're not contacting a real API.
     *
     * @author Marc Savy {@literal <msavy@redhat.com>}
     */
    class ShortcircuitApiConnection implements IApiConnection, IApiConnectionResponse {
        private boolean finished = false;
        private IAsyncHandler<Void> endHandler;
        private IAsyncResultHandler<IApiConnectionResponse> responseHandler;
        private ApiResponse response;

        public ShortcircuitApiConnection(IAsyncResultHandler<IApiConnectionResponse> handler) {
            responseHandler = handler;

            response = new ApiResponse();
            response.setCode(200);
            response.setMessage("OK"); //$NON-NLS-1$
            response.setHeaders(responseHeaders);
        }

        @Override
        public void abort() {
        }

        @Override
        public void abort(Throwable t) {
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        /**
         * @see io.apiman.gateway.engine.IApiConnection#isConnected()
         */
        @Override
        public boolean isConnected() {
            return !finished;
        }

        @Override
        public void write(IApimanBuffer chunk) {
        }

        @Override
        public void end() {
            responseHandler.handle(AsyncResultImpl.<IApiConnectionResponse> create(this));
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
        public ApiResponse getHead() {
            return response;
        }

    }
}