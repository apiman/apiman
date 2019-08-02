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
package io.apiman.test.policies;

import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.impl.ByteBufferFactoryComponent;
import io.apiman.gateway.engine.impl.DefaultComponentRegistry;
import io.apiman.gateway.engine.impl.DefaultEngineFactory;
import io.apiman.gateway.engine.policy.IPolicy;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * A junit test runner that makes it really easy to unit test custom policy
 * implementations.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class PolicyTester extends BlockJUnit4ClassRunner {

    private IEngine engine;
    private String orgId = "PolicyTester";
    private String apiId = "TestApi";
    private int version = 0;

    /**
     * Constructor.
     * @param testClass the test class
     * @throws InitializationError initializationerror if error on init
     */
    public PolicyTester(Class<?> testClass) throws InitializationError {
        super(testClass);
        System.out.println("=== Using apiman's PolicyTester on " + testClass);
        setTesterOnTest();
    }

    /**
     * @see org.junit.runners.BlockJUnit4ClassRunner#runChild(org.junit.runners.model.FrameworkMethod, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        publishApi(method);
        super.runChild(method, notifier);
        retireApi();
    }

    /**
     * @see org.junit.runners.ParentRunner#run(org.junit.runner.notification.RunNotifier)
     */
    @Override
    public void run(final RunNotifier notifier) {
        // For every run, we need to set up an instance of the apiman engine.
        setEngine(createEngine());
        super.run(notifier);
    }

    /**
     * Sets the engine instance on the test class, assuming the test class
     * has a static field named engine of type {@link IEngine}.
     */
    private void setTesterOnTest() {
        try {
            Field field = getTestClass().getJavaClass().getField("tester");
            if (field != null && field.getType().equals(PolicyTester.class)) {
                field.set(null, this);
            }
        } catch (Exception e) {
            return;
        }
    }

    /**
     * Publish a API configured with the correct policy and policy config.
     * @param method
     * @throws Throwable
     */
    protected void publishApi(FrameworkMethod method) {
        version++;

        try {
            // Get the policy class under test.
            TestingPolicy tp = method.getMethod().getAnnotation(TestingPolicy.class);
            if (tp == null) {
                tp = getTestClass().getJavaClass().getAnnotation(TestingPolicy.class);
            }
            if (tp == null) {
                throw new Exception("Missing test annotation @TestingPolicy.");
            }
            Class<? extends IPolicy> policyUnderTest = tp.value();

            // Get the configuration JSON to use
            Configuration config = method.getMethod().getAnnotation(Configuration.class);
            if (config == null) {
                config = getTestClass().getJavaClass().getAnnotation(Configuration.class);
            }
            if (config == null) {
                throw new Exception("Missing test annotation @Configuration.");
            }

            // Get the back end API simulator to use
            BackEndApi backEnd = method.getMethod().getAnnotation(BackEndApi.class);
            if (backEnd == null) {
                backEnd = getTestClass().getJavaClass().getAnnotation(BackEndApi.class);
            }
            Class<? extends IPolicyTestBackEndApi> backEndApi;
            if (backEnd == null) {
                backEndApi = EchoBackEndApi.class;
            } else {
                backEndApi = backEnd.value();
            }


            final Set<Throwable> errorHolder = new HashSet<>();

            Policy policy = new Policy();
            policy.setPolicyImpl("class:" + policyUnderTest.getName());
            policy.setPolicyJsonConfig(getPolicyConfiguration(config));

            Api api = new Api();
            api.setEndpoint(backEndApi.getName());
            api.setEndpointType("TEST");
            api.setOrganizationId(orgId);
            api.setApiId(apiId);
            api.setVersion(String.valueOf(version));
            api.setPublicAPI(true);
            api.setApiPolicies(Collections.singletonList(policy));
            api.setParsePayload(true);

            getEngine().getRegistry().publishApi(api, new IAsyncResultHandler<Void>() {
                @Override
                public void handle(IAsyncResult<Void> result) {
                    if (result.isError()) {
                        errorHolder.add(result.getError());
                    }
                }
            });

            if (!errorHolder.isEmpty()) {
                throw errorHolder.iterator().next();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the policy configuration as a String, based on the provided Configuration.
     *
     * @param config the Configuration test annotation
     * @return the policy configuration
     */
    private String getPolicyConfiguration(Configuration config) {
        if (StringUtils.isNotBlank(config.classpathConfigFile())) {
            try (InputStream configStream = getTestClass().getJavaClass().getClassLoader().getResourceAsStream(config.classpathConfigFile())) {
                return IOUtils.toString(configStream);

            } catch (IOException e) {
                throw new RuntimeException(String.format("Error loading policy configuration from file: %s",
                        config.classpathConfigFile()), e);
            }
        }

        // assume provided by the default annotation value
        return config.value();
    }

    /**
     * Retires the API, removing it from the engine.
     */
    protected void retireApi() {
        Api api = new Api();
        api.setOrganizationId(orgId);
        api.setApiId(apiId);
        api.setVersion(String.valueOf(version));
        getEngine().getRegistry().retireApi(api, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                // This is a good faith effort - we don't really care if it can't be retired.
            }
        });
    }

    /**
     * Creates an engine.
     * @return a new apiman engine
     */
    private IEngine createEngine() {
        DefaultEngineFactory factory = new DefaultEngineFactory() {

            @Override
            protected IConnectorFactory createConnectorFactory(IPluginRegistry pluginRegistry) {
                return new PolicyTesterConnectorFactory();
            }

            @Override
            protected IComponentRegistry createComponentRegistry(IPluginRegistry pluginRegistry) {
                return new DefaultComponentRegistry() {
                    @Override
                    protected void registerBufferFactoryComponent() {
                        addComponent(IBufferFactoryComponent.class, new ByteBufferFactoryComponent());
                    }
                };
            }

            @Override
            protected IPluginRegistry createPluginRegistry() {
                return new IPluginRegistry() {
                    @Override
                    public Future<IAsyncResult<Plugin>> loadPlugin(PluginCoordinates coordinates, IAsyncResultHandler<Plugin> handler) {
                        throw new RuntimeException("Plugins not supported.");
                    }
                };
            }

            @Override
            protected void complete() {
            }
        };
        return factory.createEngine();
    }

    /**
     * @return the engine
     */
    public IEngine getEngine() {
        return engine;
    }

    /**
     * @param engine the engine to set
     */
    private void setEngine(IEngine engine) {
        this.engine = engine;
    }

    /**
     * @return an API request
     */
    public ApiRequest createApiRequest() {
        ApiRequest request = new ApiRequest();
        request.setApiOrgId(orgId);
        request.setApiId(apiId);
        request.setApiVersion(String.valueOf(version));
        request.setTransportSecure(true);
        return request;
    }

}
