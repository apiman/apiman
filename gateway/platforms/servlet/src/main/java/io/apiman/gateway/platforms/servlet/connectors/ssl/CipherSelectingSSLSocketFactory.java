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
package io.apiman.gateway.platforms.servlet.connectors.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Wraps an {@link SSLSocketFactory} allowing specification of ciphers and protocols via an
 * {@link SSLSessionStrategy}, normally it's a per-JVM global setting.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class CipherSelectingSSLSocketFactory extends SSLSocketFactory {

    private SSLSocketFactory delegate;
    private String[] ciphers;
    private String[] protocols;

    public CipherSelectingSSLSocketFactory(SSLSocketFactory delegate, String[] ciphers,
            String[] protocols) {
        this.delegate = delegate;
        this.ciphers = ciphers;
        this.protocols = protocols;
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        SSLSocket socket = (SSLSocket)  delegate.createSocket(s, host, port, autoClose);
        prepareSSLSocket(socket);
        return socket;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        SSLSocket socket = (SSLSocket) createSocket(host, port);
        return prepareSSLSocket(socket);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        SSLSocket socket = (SSLSocket) createSocket(host, port);
        return prepareSSLSocket(socket);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        SSLSocket socket = (SSLSocket) createSocket(host, port, localHost, localPort);
        return prepareSSLSocket(socket);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
            throws IOException {
        SSLSocket socket = (SSLSocket) createSocket(address, port, localAddress, localPort);
        return prepareSSLSocket(socket);
    }

    private Socket prepareSSLSocket(SSLSocket socket) {
        //socket.setEnabledCipherSuites(ciphers);
        //socket.setEnabledProtocols(protocols);
        socket.setNeedClientAuth(true);
        return socket;
    }
}
