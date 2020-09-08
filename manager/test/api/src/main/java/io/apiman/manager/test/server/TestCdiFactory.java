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

import io.apiman.common.config.SystemPropertiesConfiguration;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.crypt.CurrentDataEncrypter;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.manager.api.beans.apis.EndpointType;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.summary.ApiNamespaceBean;
import io.apiman.manager.api.beans.summary.AvailableApiBean;
import io.apiman.manager.api.core.*;
import io.apiman.manager.api.core.crypt.DefaultDataEncrypter;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.core.logging.StandardLoggerImpl;
import io.apiman.manager.api.es.EsMetricsAccessor;
import io.apiman.manager.api.es.EsStorage;
import io.apiman.manager.api.jpa.IJpaProperties;
import io.apiman.manager.api.jpa.JpaStorage;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.security.impl.DefaultSecurityContext;
import io.apiman.manager.test.util.ManagerTestUtils;
import io.apiman.manager.test.util.ManagerTestUtils.TestType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;
import java.util.*;

/**
 * Attempt to create producer methods for CDI beans.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
@SuppressWarnings("nls")
@Named("ApimanLogFactory")
public class TestCdiFactory {

    private static final int JEST_TIMEOUT = 6000;

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
    public static INewUserBootstrapper provideNewUserBootstrapper() {
        return new INewUserBootstrapper() {
            @Override
            public void bootstrapUser(UserBean user, IStorage storage) throws StorageException {
                // Do nothing special.
            }
        };
    }

    @Produces @ApplicationScoped
    public static IJpaProperties provideJpaProperties() {
        return new IJpaProperties() {
            @Override
            public Map<String, String> getAllHibernateProperties() {
                SystemPropertiesConfiguration config = new SystemPropertiesConfiguration();
                Map<String, String> rval = new HashMap<>();
                Iterator<String> keys = config.getKeys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (key.startsWith("apiman.hibernate.")) { //$NON-NLS-1$
                        String value = config.getString(key);
                        key = key.substring("apiman.".length()); //$NON-NLS-1$
                        rval.put(key, value);
                    }
                }
                return rval;
            }
        };
    }

    @Produces @ApplicationScoped
    public static IStorage provideStorage(@New JpaStorage jpaStorage) {
        TestType testType = ManagerTestUtils.getTestType();
        if (testType == TestType.jpa) {
            return jpaStorage;
        } else if (testType == TestType.es) {
            EsStorage esStorage = new EsStorage(ManagerApiTestServer.getTestClientConfig());
            return new TestEsStorageWrapper(esStorage);
        } else {
            throw new RuntimeException("Unexpected test type: " + testType);
        }
    }

    @Produces @ApplicationScoped
    public static IStorageQuery provideStorageQuery(@New JpaStorage jpaStorage) {
        TestType testType = ManagerTestUtils.getTestType();
        if (testType == TestType.jpa) {
            return jpaStorage;
        } else if (testType == TestType.es) {
            EsStorage esStorage = new EsStorage(ManagerApiTestServer.getTestClientConfig());
            return new TestEsStorageQueryWrapper(esStorage);
        } else {
            throw new RuntimeException("Unexpected test type: " + testType);
        }
    }

    @Produces @ApplicationScoped
    public static IApiKeyGenerator provideApiKeyGenerator(@New UuidApiKeyGenerator uuidApiKeyGen) {
        return uuidApiKeyGen;
    }

    @Produces @ApplicationScoped
    public static IDataEncrypter provideDataEncrypter(@New DefaultDataEncrypter defaultEncrypter) {
        CurrentDataEncrypter.instance = defaultEncrypter;
        return defaultEncrypter;
    }

    @Produces @ApplicationScoped
    public static IApiCatalog provideApiCatalog(IPluginRegistry pluginRegistry) {
        return new IApiCatalog() {
            /**
             * @see io.apiman.manager.api.core.IApiCatalog#search(java.lang.String, java.lang.String)
             */
            @Override
            public List<AvailableApiBean> search(String keyword, String namespace) {
                List<AvailableApiBean> rval = new ArrayList<>();
                AvailableApiBean asb = new AvailableApiBean();
                asb.setName("Test API 1");
                asb.setDescription("The first test API.");
                asb.setEndpoint("http://api1.example.org/api");
                asb.setEndpointType(EndpointType.rest);
                rval.add(asb);

                asb = new AvailableApiBean();
                asb.setName("Test API 2");
                asb.setDescription("The second test API.");
                asb.setEndpoint("http://api2.example.org/api");
                asb.setEndpointType(EndpointType.rest);
                rval.add(asb);

                return rval;
            }
            /**
             * @see io.apiman.manager.api.core.IApiCatalog#getNamespaces(java.lang.String)
             */
            @Override
            public List<ApiNamespaceBean> getNamespaces(String currentUser) {
                List<ApiNamespaceBean> rval = new ArrayList<>();

                ApiNamespaceBean bean = new ApiNamespaceBean();
                bean.setCurrent(true);
                bean.setName("current");
                bean.setOwnedByUser(true);
                rval.add(bean);

                bean = new ApiNamespaceBean();
                bean.setCurrent(false);
                bean.setName("ns1");
                bean.setOwnedByUser(true);
                rval.add(bean);

                bean = new ApiNamespaceBean();
                bean.setCurrent(false);
                bean.setName("ns2");
                bean.setOwnedByUser(false);
                rval.add(bean);

                bean = new ApiNamespaceBean();
                bean.setCurrent(false);
                bean.setName("ns3");
                bean.setOwnedByUser(false);
                rval.add(bean);

                return rval;
            }
        };
    }

    @Produces @ApplicationScoped
    public static IMetricsAccessor provideMetricsAccessor() {
        boolean enableEsMetrics = "true".equals(System.getProperty("apiman-test.es-metrics", "false"));
        if (enableEsMetrics) {
            return new EsMetricsAccessor(ManagerApiTestServer.ES_CLIENT);
        } else {
            return new TestMetricsAccessor();
        }
    }
}
