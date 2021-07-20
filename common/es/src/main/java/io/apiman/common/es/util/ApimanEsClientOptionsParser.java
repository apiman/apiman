/*
 * Copyright 2021 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apiman.common.es.util;

import io.apiman.common.config.options.GenericOptionsParser;
import io.apiman.common.config.options.Predicates;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Predicate;

/**
 * Parser for Elasticsearch client options.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
final class ApimanEsClientOptionsParser extends GenericOptionsParser {

    static final int DEFAULT_PORT = 9200;
    static final String DEFAULT_PROTOCOL = "http";
    static final int DEFAULT_TIMEOUT_MSECS = 10000;
    static final int DEFAULT_POLLING_TIME_SECS = 600;

    private static final Predicate<Integer> PREDICATE_GTE_MINUS_ONE = v -> v >= -1;

    private final String defaultIndexPrefix;
    private String indexNamePrefix;
    private String host;
    private int port;
    private String protocol;
    private boolean initialize;
    private String username;
    private String password;
    private int timeout;
    private long pollingTime;

    /**
     * Constructor.
     *
     * @param options            raw options passed to component
     * @param defaultIndexPrefix default Elasticsearch index name prefix (e.g: myIndexPrefix-indexName)
     */
    public ApimanEsClientOptionsParser(Map<String, String> options, String defaultIndexPrefix) {
        super(options);
        this.defaultIndexPrefix = defaultIndexPrefix;
        parseOptions();
    }

    private void parseOptions() {
        this.indexNamePrefix = getString(
            keys("client.indexPrefix"),
            defaultIndexPrefix,
            Predicates.noWhitespace(),
            Predicates.noWhitespaceMsg()
        );

        this.host = getRequiredString(
            keys("client.host"),
            Predicates.noWhitespace(),
            Predicates.noWhitespaceMsg()
        );

        this.port = getInt(
            keys("client.port"),
            DEFAULT_PORT,
            Predicates.greaterThanZeroInt(),
            Predicates.greaterThanZeroMsg()
        );

        this.protocol = getString(
            keys("client.protocol"),
            DEFAULT_PROTOCOL,
            Predicates.matchesAny("http", "https"),
            Predicates.matchesAnyMsg("http", "https")
        );

        this.initialize = getBool(keys("client.initialize"), true);

        this.username = getString(
            keys("client.username"),
            null,
            Predicates.noWhitespace(),
            Predicates.noWhitespaceMsg()
        );

        this.password = getString(
            keys("client.password"),
            null,
            Predicates.anyOk(),
            null
        );

        this.timeout = getInt(
            keys("client.timeout"),
            DEFAULT_TIMEOUT_MSECS,
            PREDICATE_GTE_MINUS_ONE,
            "must be -1 or greater, where -1 is 'default', 0 is infinite,"
                + " and positive integers are milliseconds"
        );

        this.pollingTime = getLong(
            keys("client.pollingTime", "client.polling.time"),
            DEFAULT_POLLING_TIME_SECS,
            Predicates.greaterThanZeroLong(),
            Predicates.greaterThanZeroMsg()
        );
    }

    public String getDefaultIndexPrefix() {
        return defaultIndexPrefix;
    }

    public String getIndexNamePrefix() {
        return indexNamePrefix;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public boolean isInitialize() {
        return initialize;
    }

    public Optional<UsernameAndPassword> getUsernameAndPassword() {
        if (!hasUsernameAndPassword()) {
            return Optional.empty();
        }
        return Optional.of(new UsernameAndPassword(username, password));
    }

    public int getTimeout() {
        return timeout;
    }

    public long getPollingTime() {
        return pollingTime;
    }

    public boolean hasUsernameAndPassword() {
        return username != null && password != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApimanEsClientOptionsParser that = (ApimanEsClientOptionsParser) o;
        return port == that.port && initialize == that.initialize && timeout == that.timeout
            && pollingTime == that.pollingTime && Objects.equals(defaultIndexPrefix,
            that.defaultIndexPrefix) && Objects.equals(indexNamePrefix, that.indexNamePrefix)
            && Objects.equals(host, that.host) && Objects.equals(protocol, that.protocol)
            && Objects.equals(username, that.username) && Objects.equals(password,
            that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultIndexPrefix, indexNamePrefix, host, port, protocol, initialize, username,
            password, timeout, pollingTime);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApimanEsClientOptionsParser.class.getSimpleName() + "[", "]")
            .add("defaultIndexPrefix='" + defaultIndexPrefix + "'")
            .add("indexNamePrefix='" + indexNamePrefix + "'")
            .add("host='" + host + "'")
            .add("port=" + port)
            .add("protocol='" + protocol + "'")
            .add("initialize=" + initialize)
            .add("username='" + username + "'")
            .add("password='****'")
            .add("timeout=" + timeout)
            .add("pollingTime=" + pollingTime)
            .toString();
    }

    /**
     * Username and password container, can be used with try-with-resources to overwrite in-memory password,
     * as per best practices. But, most places just use a plain string, so it's probably not going to make
     * much of a difference!
     */
    public static final class UsernameAndPassword implements Closeable {
        private final String username;
        private final byte[] password;

        UsernameAndPassword(String username, String password) {
            this.username = username;
            this.password = password.getBytes(StandardCharsets.UTF_8);
        }

        UsernameAndPassword(String username, byte[] password) {
            this.username = username;
            this.password = password;
        }

        public byte[] getPassword() {
            return password;
        }

        public String getPasswordAsString() {
            return new String(password, StandardCharsets.UTF_8);
        }

        public String getUsername() {
            return username;
        }

        @Override
        public void close() throws IOException {
            Arrays.fill(password, (byte) 0xa);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", UsernameAndPassword.class.getSimpleName() + "[", "]")
                .add("username='" + username + "'")
                .add("password=***")
                .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            UsernameAndPassword that = (UsernameAndPassword) o;
            return Objects.equals(username, that.username) && Arrays
                .equals(password, that.password);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(username);
            result = 31 * result + Arrays.hashCode(password);
            return result;
        }
    }
}
