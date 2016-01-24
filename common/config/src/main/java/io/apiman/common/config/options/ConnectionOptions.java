/*
 * Copyright 2016 Pete Cornish
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
package io.apiman.common.config.options;

import java.util.Map;

/**
 * Options parser for connection settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class ConnectionOptions extends AbstractOptions {
    public static final String PREFIX = "connection."; //$NON-NLS-1$
    public static final String FOLLOW_REDIRECTS = PREFIX + "followRedirects"; //$NON-NLS-1$
    public static final String READ_TIMEOUT = PREFIX + "readTimeout"; //$NON-NLS-1$
    public static final String CONNECT_TIMEOUT = PREFIX + "connectTimeout"; //$NON-NLS-1$

    /**
     * Whether to automatically follow redirects.
     */
    private boolean followRedirects;

    /**
     * The read timeout in millis.
     */
    private int readTimeout;

    /**
     * The connect timeout in millis.
     */
    private int connectTimeout;

    /**
     * Constructor. Parses options immediately.
     * @param options the options
     */
    public ConnectionOptions(Map<String, String> options) {
        super(options);
    }

    /**
     * @see AbstractOptions#parse(Map)
     */
    @Override
    protected void parse(Map<String, String> options) {
        setFollowRedirects(parseBool(options, FOLLOW_REDIRECTS, true));
        setReadTimeout(parseInt(options, READ_TIMEOUT, 15000));
        setConnectTimeout(parseInt(options, CONNECT_TIMEOUT, 10000));
    }

    /**
     * @return the followRedirects
     */
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    /**
     * @param followRedirects the followRedirects to set
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
     * @return the readTimeout
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * @param readTimeout the readTimeout to set
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * @return the connectTimeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * @param connectTimeout the connectTimeout to set
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
