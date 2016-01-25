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

package io.apiman.common.config.options;

import java.util.Map;

/**
 * Models http connector options such as timeouts.
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpConnectorOptions extends AbstractOptions {
    
    private static final int DEFAULT_READ_TIMEOUT = 30;
    private static final int DEFAULT_WRITE_TIMEOUT = 30;
    private static final int DEFAULT_CONNECT_TIMEOUT = 10;
    private static final boolean DEFAULT_FOLLOW_REDIRECTS = true;

    private int readTimeout;
    private int writeTimeout;
    private int connectTimeout;
    private boolean followRedirects;

    /**
     * Constructor.
     * @param options
     */
    public HttpConnectorOptions(Map<String, String> options) {
        super(options);
    }
    
    /**
     * @see io.apiman.common.config.options.AbstractOptions#parse(java.util.Map)
     */
    @Override
    protected void parse(Map<String, String> options) {
        String read = options.get("http.timeouts.read"); //$NON-NLS-1$
        String write = options.get("http.timeouts.write"); //$NON-NLS-1$
        String connect = options.get("http.timeouts.connect"); //$NON-NLS-1$
        String redirects = options.get("http.followRedirects"); //$NON-NLS-1$
        if (read != null) {
            setReadTimeout(Integer.parseInt(read));
        } else {
            setReadTimeout(DEFAULT_READ_TIMEOUT);
        }
        if (write != null) {
            setWriteTimeout(Integer.parseInt(write));
        } else {
            setWriteTimeout(DEFAULT_WRITE_TIMEOUT);
        }
        if (connect != null) {
            setConnectTimeout(Integer.parseInt(connect));
        } else {
            setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        }
        if (redirects != null) {
            setFollowRedirects(Boolean.parseBoolean(redirects));
        } else {
            setFollowRedirects(DEFAULT_FOLLOW_REDIRECTS);
        }
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
     * @return the writeTimeout
     */
    public int getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * @param writeTimeout the writeTimeout to set
     */
    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
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
}
