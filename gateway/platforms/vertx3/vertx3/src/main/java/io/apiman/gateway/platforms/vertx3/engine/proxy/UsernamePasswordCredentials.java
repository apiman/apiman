/*
 * Copyright 2021 Scheer PAS Schweiz AG
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
package io.apiman.gateway.platforms.vertx3.engine.proxy;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Simple username and password credentials.
 *
 * When used with try-with-resources or #close the password array will be overwritten with
 * <tt>\ u0000</tt> characters.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class UsernamePasswordCredentials extends AbstractCredentials {

    private final char[] password;
    private final String principle;

    public UsernamePasswordCredentials(String username, String password) {
        this.principle = username;
        this.password = password.toCharArray();
    }

    public UsernamePasswordCredentials(String username, char[] password) {
        this.principle = username;
        this.password = password;
    }

    @Override
    public String getPrinciple() {
        return principle;
    }

    public char[] getPassword() {
        return password;
    }

    public String getPasswordAsString() {
        return new String(password);
    }

    @Override
    public void close() throws IOException {
        Arrays.fill(password, '\u0000');
    }

    @Override
    public String toString() {
        return "UsernamePasswordCredentials{" +
            "password=***" +
            ", principle='" + principle + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsernamePasswordCredentials that = (UsernamePasswordCredentials) o;
        return Arrays.equals(password, that.password) && Objects
            .equals(principle, that.principle);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(principle);
        result = 31 * result + Arrays.hashCode(password);
        return result;
    }
}
