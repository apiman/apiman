/*
 * Copyright 2022. Black Parrot Labs Ltd
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

package io.apiman.gateway.engine.influxdb;

import io.apiman.common.config.options.GenericOptionsParser;
import io.apiman.common.config.options.Predicates;

import java.util.Map;
import java.util.StringJoiner;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class InfluxDbMetricsOptionsParser extends GenericOptionsParser {
    private static final String INFLUX_ENDPOINT = "endpoint";
    private static final String USER = "username";
    private static final String PWORD = "password";
    private static final String DATABASE = "database";
    private static final String RETENTION_POLICY = "retentionPolicy";
    private static final String SERIES_NAME = "measurement";

    private static final String GENERATOR_NAME = "generatorName";

    private final String influxEndpoint;
    private final String dbName;
    private final String retentionPolicy;
    private final String seriesName;
    private final String username;
    private final String password;
    private final String token;
    private final String generatorName;
    private final int queueCapacity;
    private final int maxBatchSize;

    public InfluxDbMetricsOptionsParser(Map<String, String> options) {
        super(options);
        this.influxEndpoint = super.getRequiredString(keys(INFLUX_ENDPOINT), Predicates.noWhitespace(), Predicates.noWhitespaceMsg());
        this.dbName = super.getRequiredString(keys(DATABASE), Predicates.noWhitespace(), Predicates.noWhitespaceMsg());
        this.retentionPolicy = super.getString(keys(RETENTION_POLICY), null, Predicates.noWhitespace(), Predicates.noWhitespaceMsg());
        this.seriesName =  super.getRequiredString(keys(SERIES_NAME),  Predicates.noWhitespace(), Predicates.noWhitespaceMsg());
        this.username =  super.getString(keys(USER), null, Predicates.anyOk(), "");
        this.password =  super.getString(keys(PWORD), null, Predicates.anyOk(), "");
        this.token = super.getString(keys("token"), null, Predicates.noWhitespace(), Predicates.noWhitespaceMsg());
        this.generatorName = super.getString(keys(GENERATOR_NAME), "apiman-gateway", Predicates.noWhitespace(), Predicates.noWhitespaceMsg());
        this.queueCapacity = super.getInt(keys("queue.size"), 10_000, Predicates.greaterThanZeroInt(), Predicates.greaterThanZeroMsg());
        this.maxBatchSize = super.getInt(keys("batch.size"), 5_000, Predicates.greaterThanZeroInt(), Predicates.greaterThanZeroMsg());
    }

    public String getInfluxEndpoint() {
        return influxEndpoint;
    }

    public String getDbName() {
        return dbName;
    }

    public String getRetentionPolicy() {
        return retentionPolicy;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public String getGeneratorName() {
        return generatorName;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InfluxDbMetricsOptionsParser.class.getSimpleName() + "[", "]")
                       .add("influxEndpoint='" + influxEndpoint + "'")
                       .add("dbName='" + dbName + "'")
                       .add("retentionPolicy='" + retentionPolicy + "'")
                       .add("seriesName='" + seriesName + "'")
                       .add("username='" + username + "'")
                       .add("password='***'")
                       .add("token='***'")
                       .add("generatorName='" + generatorName + "'")
                       .add("queueCapacity='" + queueCapacity + "'")
                       .add("maxBatchSize='" + maxBatchSize + "'")
                       .toString();
    }
}
