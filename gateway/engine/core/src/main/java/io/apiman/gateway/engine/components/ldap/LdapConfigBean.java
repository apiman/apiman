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
package io.apiman.gateway.engine.components.ldap;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class LdapConfigBean {
    // DN to authenticate with server with
    private String bindDn;

    // Password of user
    private String bindPassword;

    // Host of LDAP server
    private String host;

    // Port of LDAP server
    private int port = 389;

    public LdapConfigBean() {}
    /**
     * @return the password
     */
    public String getBindPassword() {
        return bindPassword;
    }

    /**
     * @param bindPassword the password to set
     */
    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }

    /**
     * @return the bindDn
     */
    public String getBindDn() {
        return bindDn;
    }

    /**
     * @param bindDn the bindDn to set
     */
    public void setBindDn(String bindDn) {
        this.bindDn = bindDn;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }
}
