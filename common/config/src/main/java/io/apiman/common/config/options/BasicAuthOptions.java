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
 * Options parser for BASIC authentication endpoint security.
 */
public class BasicAuthOptions extends AbstractOptions {
    public static final String PREFIX = "basic-auth."; //$NON-NLS-1$
    public static final String BASIC_USERNAME = PREFIX + "username"; //$NON-NLS-1$
    public static final String BASIC_PASSWORD = PREFIX + "password"; //$NON-NLS-1$
    public static final String BASIC_REQUIRE_SSL = PREFIX + "requireSSL"; //$NON-NLS-1$

    private String username;
    private String password;
    private boolean requireSSL;

    /**
     * Constructor. Parses options immediately.
     * @param options the options
     */
    public BasicAuthOptions(Map<String, String> options) {
        super(options);
    }

    /**
     * @see io.apiman.common.config.options.AbstractOptions#parse(java.util.Map)
     */
    @Override
    protected void parse(Map<String, String> options) {
        setUsername(getVar(options, BASIC_USERNAME));
        setPassword(getVar(options, BASIC_PASSWORD));
        setRequireSSL(parseBool(options, BASIC_REQUIRE_SSL, true));
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the requireSSL
     */
    public boolean isRequireSSL() {
        return requireSSL;
    }

    /**
     * @param requireSSL the requireSSL to set
     */
    public void setRequireSSL(boolean requireSSL) {
        this.requireSSL = requireSSL;
    }
}
