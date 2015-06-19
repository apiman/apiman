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
package io.apiman.manager.test.server;

import io.apiman.manager.api.core.IApiKeyGenerator;
import io.apiman.manager.api.core.IIdmStorage;
import io.apiman.manager.api.core.IMetricsAccessor;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.UuidApiKeyGenerator;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.core.logging.IApimanLogger;
import io.apiman.manager.api.core.logging.StandardLoggerImpl;
import io.apiman.manager.api.core.noop.NoOpMetricsAccessor;
import io.apiman.manager.api.es.ESMetricsAccessor;
import io.apiman.manager.api.es.EsStorage;
import io.apiman.manager.api.jpa.JpaStorage;
import io.apiman.manager.api.jpa.roles.JpaIdmStorage;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.security.impl.DefaultSecurityContext;
import io.apiman.manager.test.util.ManagerTestUtils;
import io.apiman.manager.test.util.ManagerTestUtils.TestType;
import io.searchbox.client.JestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;

/**
 * Attempt to create producer methods for CDI beans.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
@SuppressWarnings("nls")
@Named("ApimanLogFactory")
public class TestCdiFactory {

    @Produces @ApplicationScoped
    public static ISecurityContext provideSecurityContext(@New DefaultSecurityContext defaultSC) {
        return defaultSC;
    }

    @Produces @ApimanLogger
    public static IApimanLogger provideLogger(InjectionPoint injectionPoint) {
        ApimanLogger logger = injectionPoint.getAnnotated().getAnnotation(ApimanLogger.class);
        Class<?> klazz = logger.value();
        return new StandardLoggerImpl().createLogger(klazz);
    }

    @Produces @ApplicationScoped
    public static IStorage provideStorage(@New JpaStorage jpaStorage, @New EsStorage esStorage) {
        TestType testType = ManagerTestUtils.getTestType();
        if (testType == TestType.jpa) {
            return jpaStorage;
        } else if (testType == TestType.es) {
            esStorage.initialize();
            return new TestEsStorageWrapper(ManagerApiTestServer.ES_CLIENT, esStorage);
        } else {
            throw new RuntimeException("Unexpected test type: " + testType);
        }
    }

    @Produces @ApplicationScoped
    public static IStorageQuery provideStorageQuery(@New JpaStorage jpaStorage, @New EsStorage esStorage) {
        TestType testType = ManagerTestUtils.getTestType();
        if (testType == TestType.jpa) {
            return jpaStorage;
        } else if (testType == TestType.es) {
            esStorage.initialize();
            return new TestEsStorageQueryWrapper(ManagerApiTestServer.ES_CLIENT, esStorage);
        } else {
            throw new RuntimeException("Unexpected test type: " + testType);
        }
    }

    @Produces @ApplicationScoped
    public static IApiKeyGenerator provideApiKeyGenerator(@New UuidApiKeyGenerator uuidApiKeyGen) {
        return uuidApiKeyGen;
    }

    @Produces @ApplicationScoped
    public static IIdmStorage provideIdmStorage(@New JpaIdmStorage jpaIdmStorage, @New EsStorage esStorage) {
        TestType testType = ManagerTestUtils.getTestType();
        if (testType == TestType.jpa) {
            return jpaIdmStorage;
        } else if (testType == TestType.es) {
            esStorage.initialize();
            return new TestEsIdmStorageWrapper(ManagerApiTestServer.ES_CLIENT, esStorage);
        } else {
            throw new RuntimeException("Unexpected test type: " + testType);
        }
    }

    @Produces @ApplicationScoped @Named("storage")
    public static JestClient provideStorageJestClient() {
        TestType testType = ManagerTestUtils.getTestType();
        if (testType == TestType.jpa) {
            return null;
        } else if (testType == TestType.es) {
            return ManagerApiTestServer.ES_CLIENT;
        } else {
            throw new RuntimeException("Unexpected test type: " + testType);
        }
    }

    @Produces @ApplicationScoped @Named("metrics")
    public static JestClient provideMetricsJestClient() {
        TestType testType = ManagerTestUtils.getTestType();
        if (testType == TestType.jpa) {
            return null;
        } else if (testType == TestType.es) {
            return ManagerApiTestServer.ES_CLIENT;
        } else {
            throw new RuntimeException("Unexpected test type: " + testType);
        }
    }

    @Produces @ApplicationScoped
    public static IMetricsAccessor provideMetricsAccessor(@New NoOpMetricsAccessor noopMetrics, @New ESMetricsAccessor esMetrics) {
        TestType testType = ManagerTestUtils.getTestType();
        if (testType == TestType.jpa) {
            // Currently do not support metrics in the JPA test environment
            return noopMetrics;
        } else if (testType == TestType.es) {
            return esMetrics;
        } else {
            throw new RuntimeException("Unexpected test type: " + testType);
        }
    }
}
