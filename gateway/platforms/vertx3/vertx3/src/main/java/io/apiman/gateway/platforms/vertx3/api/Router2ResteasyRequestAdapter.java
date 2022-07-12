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

import java.util.Map;
import java.util.Set;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

import io.netty.handler.codec.DecoderResult;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpFrame;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerFileUpload;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.StreamPriority;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;

/**
* @author Marc Savy {@literal <marc@rhymewithgravy.com>}
*/
public class Router2ResteasyRequestAdapter implements HttpServerRequest {

    private HttpServerRequest request;
    private RoutingContext context;

    public Router2ResteasyRequestAdapter(RoutingContext context) {
        this.request = context.request();
        this.context = context;
    }

    @Override
    public HttpVersion version() {
        return request.version();
    }

    @Override
    public String uri() {
        return request.uri();
    }

    @Override
    public HttpServerRequest uploadHandler(@Nullable Handler<HttpServerFileUpload> uploadHandler) {
        request.uploadHandler(uploadHandler);
        return this;
    }

    @Override
    public HttpServerRequest setExpectMultipart(boolean expect) {
        request.setExpectMultipart(expect);
        return this;
    }

    @Override
    public @Nullable String scheme() {
        return request.scheme();
    }

    @Override
    public HttpServerRequest resume() {
        request.resume();
        return this;
    }

    @Override
    public HttpServerRequest fetch(long l) {
        return request.fetch(l);
    }

    @Override
    public HttpServerResponse response() {
        return request.response();
    }

    @Override
    public SocketAddress remoteAddress() {
        return request.remoteAddress();
    }

    @Override
    public @Nullable String query() {
        return request.query();
    }

    @Override
    public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
        return request.peerCertificateChain();
    }

    @Override
    public HttpServerRequest pause() {
        request.pause();
        return this;
    }

    @Override
    public @Nullable String path() {
        return request.path();
    }

    @Override
    public MultiMap params() {
        return request.params();
    }

    @Override
    public HttpMethod method() {
        return request.method();
    }

    @Override
    public SocketAddress localAddress() {
        return request.localAddress();
    }

    @Override
    public SSLSession sslSession() {
        return request.sslSession();
    }

    @Override
    public boolean isSSL() {
        return request.isSSL();
    }

    @Override
    public boolean isExpectMultipart() {
        return request.isExpectMultipart();
    }

    @Override
    public boolean isEnded() {
        return request.isEnded();
    }

    @Override
    public @Nullable String host() {
        return request.host();
    }

    @Override
    public long bytesRead() {
        return request.bytesRead();
    }

    @Override
    public MultiMap headers() {
        return request.headers();
    }

    // Return the cached body (we're assuming it's there by current design).
    @Override
    public HttpServerRequest handler(Handler<Buffer> handler) {
        handler.handle(context.getBody());
        return this;
    }

    @Override
    public @Nullable String getParam(String paramName) {
        return request.getParam(paramName);
    }

    @Override
    public String getHeader(CharSequence headerName) {
        return request.getHeader(headerName);
    }

    @Override
    public HttpServerRequest setParamsCharset(String charset) {
        return request.setParamsCharset(charset);
    }

    @Override
    public String getParamsCharset() {
        return request.getParamsCharset();
    }

    @Override
    public @Nullable String getHeader(String headerName) {
        return request.getHeader(headerName);
    }

    @Override
    public @Nullable String getFormAttribute(String attributeName) {
        return request.getFormAttribute(attributeName);
    }

    @Override
    public Future<ServerWebSocket> toWebSocket() {
        return request.toWebSocket();
    }

    @Override
    public MultiMap formAttributes() {
        return request.formAttributes();
    }

    @Override
    public HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
        request.exceptionHandler(handler);
        return this;
    }

    @Override
    public HttpServerRequest endHandler(Handler<Void> endHandler) {
        endHandler.handle((Void) null);
        return this;
    }

    @Override
    public HttpServerRequest customFrameHandler(Handler<HttpFrame> handler) {
        request.customFrameHandler(handler);
        return this;
    }

    @Override
    public HttpConnection connection() {
        return request.connection();
    }

    @Override
    public HttpServerRequest streamPriorityHandler(Handler<StreamPriority> handler) {
        return request.streamPriorityHandler(handler);
    }

    @Override
    public DecoderResult decoderResult() {
        return request.decoderResult();
    }

    @Override
    public @Nullable Cookie getCookie(String name) {
        return request.getCookie(name);
    }

    @Override
    public @Nullable Cookie getCookie(String name, String domain, String path) {
        return request.getCookie(name, domain, path);
    }

    @Override
    public int cookieCount() {
        return request.cookieCount();
    }

    @Override
    public Map<String, Cookie> cookieMap() {
        return request.cookieMap();
    }

    @Override
    public Set<Cookie> cookies(String name) {
        return request.cookies(name);
    }

    @Override
    public Set<Cookie> cookies() {
        return request.cookies();
    }

    @Override
    public String absoluteURI() {
        return request.absoluteURI();
    }

    @Override
    public Future<Buffer> body() {
        if (context.getBody() == null) {
            return Future.succeededFuture(Buffer.buffer());
        } else {
            return Future.succeededFuture(context.getBody());
        }
    }

    @Override
    public Future<Void> end() {
        return request.end();
    }

    @Override
    public Future<NetSocket> toNetSocket() {
        return request.toNetSocket();
    }

}
