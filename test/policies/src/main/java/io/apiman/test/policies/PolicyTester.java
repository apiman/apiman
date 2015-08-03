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
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.impl.ByteBufferFactoryComponent;
import io.apiman.gateway.engine.impl.DefaultComponentRegistry;
import io.apiman.gateway.engine.impl.DefaultEngineFactory;
import io.apiman.gateway.engine.policy.IPolicy;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

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
    private String serviceId = "TestService";
    private int version = 0;

    /**
     * Constructor.
     * @param testClass
     * @throws InitializationError
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
        publishService(method);
        super.runChild(method, notifier);
        retireService();
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
     * Publish a service configured with the correct policy and policy config.
     * @param method
     * @throws Throwable
     */
    protected void publishService(FrameworkMethod method) {
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

            // Get the back end service simulator to use
            BackEndService backEnd = method.getMethod().getAnnotation(BackEndService.class);
            if (backEnd == null) {
                backEnd = getTestClass().getJavaClass().getAnnotation(BackEndService.class);
            }
            Class<? extends IPolicyTestBackEndService> backEndService = null;
            if (backEnd == null) {
                backEndService = EchoBackEndService.class;
            } else {
                backEndService = backEnd.value();
            }


            final Set<Throwable> errorHolder = new HashSet<>();

            Policy policy = new Policy();
            policy.setPolicyImpl("class:" + policyUnderTest.getName());
            policy.setPolicyJsonConfig(config.value());

            Service service = new Service();
            service.setEndpoint(backEndService.getName());
            service.setEndpointType("TEST");
            service.setOrganizationId(orgId);
            service.setServiceId(serviceId);
            service.setVersion(String.valueOf(version));
            service.setPublicService(true);
            service.setServicePolicies(Collections.singletonList(policy));

            getEngine().getRegistry().publishService(service, new IAsyncResultHandler<Void>() {
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
     * Retires the service, removing it from the engine.
     */
    protected void retireService() {
        Service service = new Service();
        service.setOrganizationId(orgId);
        service.setServiceId(serviceId);
        service.setVersion(String.valueOf(version));
        getEngine().getRegistry().retireService(service, new IAsyncResultHandler<Void>() {
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
     * @return a service request
     */
    public ServiceRequest createServiceRequest() {
        ServiceRequest request = new ServiceRequest();
        request.setServiceOrgId(orgId);
        request.setServiceId(serviceId);
        request.setServiceVersion(String.valueOf(version));
        request.setTransportSecure(true);
        return request;
    }

}
