/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.engine.policy;

import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginClassLoader;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.common.plugin.PluginUtils;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.exceptions.PolicyNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of the {@link IPolicyFactory} interface.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyFactoryImpl implements IPolicyFactory {

    private IPluginRegistry pluginRegistry;
    private Map<String, IPolicy> policyCache = new HashMap<>();
    private Map<String, Object> policyConfigCache = new HashMap<>();
    private final boolean reloadSnapshots;

    /**
     * Constructor.
     * @param config
     */
    public PolicyFactoryImpl(Map<String, String> config) {
        reloadSnapshots = "true".equals(config.get("reload-snapshots")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicyFactory#setPluginRegistry(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public void setPluginRegistry(IPluginRegistry pluginRegistry) {
        this.pluginRegistry = pluginRegistry;
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicyFactory#loadConfig(io.apiman.gateway.engine.policy.IPolicy, java.lang.String, java.lang.String)
     */
    @Override
    public Object loadConfig(IPolicy policy, String policySpec, String configData) {
        synchronized (policyConfigCache) {
            String cacheKey = policySpec + "||" + configData; //$NON-NLS-1$
            if (policyConfigCache.containsKey(cacheKey)) {
                return policyConfigCache.get(cacheKey);
            }
            Object config;
            
            ClassLoader oldCtxLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(policy.getClass().getClassLoader());
                config = policy.parseConfiguration(configData);
            } finally {
                Thread.currentThread().setContextClassLoader(oldCtxLoader);
            }

            // Note: don't cache configuration objects for snapshot versions of policies.
            if (!policySpec.contains("-SNAPSHOT")) { //$NON-NLS-1$
                policyConfigCache.put(cacheKey, config);
            }
            return config;
        }
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicyFactory#loadPolicy(java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void loadPolicy(String policyImpl, IAsyncResultHandler<IPolicy> handler) {
        if (policyImpl == null) {
            handler.handle(AsyncResultImpl.<IPolicy>create(new PolicyNotFoundException(policyImpl)));
            return;
        }

        // Not synchronized - don't care if we create 2 or 3 of these, it's not worth
        // the synchronization overhead to protect against that.
        if (policyCache.containsKey(policyImpl)) {
            handler.handle(AsyncResultImpl.create(policyCache.get(policyImpl)));
            return;
        }

        // Handle the various policyImpl formats.  Valid formats include:
        //   class:fullyQualifiedClassname - the class is expected to be on the classpath
        if (policyImpl.startsWith("class:")) { //$NON-NLS-1$
            doLoadFromClasspath(policyImpl, handler);
        } else if (policyImpl.startsWith("plugin:")) { //$NON-NLS-1$
            doLoadFromPlugin(policyImpl, handler);
        } else {
            handler.handle(AsyncResultImpl.<IPolicy>create(new PolicyNotFoundException(policyImpl)));
        }
    }

    /**
     * Loads a policy from a class on the classpath.
     * @param policyImpl
     * @param handler
     */
    protected void doLoadFromClasspath(String policyImpl, IAsyncResultHandler<IPolicy> handler) {
        IPolicy rval;
        String classname = policyImpl.substring(6);
        Class<?> c = null;

        // First try a simple Class.forName()
        try { c = Class.forName(classname); } catch (ClassNotFoundException e) { }
        // Didn't work?  Try using this class's classloader.
        if (c == null) {
            try { c = getClass().getClassLoader().loadClass(classname); } catch (ClassNotFoundException e) { }
        }
        // Still didn't work?  Try the thread's context classloader.
        if (c == null) {
            try { c = Thread.currentThread().getContextClassLoader().loadClass(classname); } catch (ClassNotFoundException e) { }
        }

        if (c == null) {
            handler.handle(AsyncResultImpl.<IPolicy>create(new PolicyNotFoundException(classname)));
            return;
        }

        try {
            rval = (IPolicy) c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            handler.handle(AsyncResultImpl.<IPolicy>create(new PolicyNotFoundException(policyImpl, e)));
            return;
        }
        policyCache.put(policyImpl, rval);
        handler.handle(AsyncResultImpl.create(rval));
        return;
    }

    /**
     * Loads a policy from a plugin.
     * @param policyImpl
     * @param handler
     */
    private void doLoadFromPlugin(final String policyImpl, final IAsyncResultHandler<IPolicy> handler) {
        PluginCoordinates coordinates = PluginCoordinates.fromPolicySpec(policyImpl);
        if (coordinates == null) {
            handler.handle(AsyncResultImpl.<IPolicy>create(new PolicyNotFoundException(policyImpl)));
            return;
        }
        int ssidx = policyImpl.indexOf('/');
        if (ssidx == -1) {
            handler.handle(AsyncResultImpl.<IPolicy>create(new PolicyNotFoundException(policyImpl)));
            return;
        }
        final String classname = policyImpl.substring(ssidx + 1);
        final boolean isSnapshot = reloadSnapshots && PluginUtils.isSnapshot(coordinates);
        this.pluginRegistry.loadPlugin(coordinates, new IAsyncResultHandler<Plugin>() {
            @Override
            public void handle(IAsyncResult<Plugin> result) {
                if (result.isSuccess()) {
                    IPolicy rval;
                    Plugin plugin = result.getResult();
                    PluginClassLoader pluginClassLoader = plugin.getLoader();
                    ClassLoader oldCtxLoader = Thread.currentThread().getContextClassLoader();
                    try {
                        Thread.currentThread().setContextClassLoader(pluginClassLoader);
                        Class<?> c = pluginClassLoader.loadClass(classname);
                        rval = (IPolicy) c.newInstance();
                    } catch (Exception e) {
                        handler.handle(AsyncResultImpl.<IPolicy>create(new PolicyNotFoundException(policyImpl, e)));
                        return;
                    } finally {
                        Thread.currentThread().setContextClassLoader(oldCtxLoader);
                    }

                    if (!isSnapshot) {
                        policyCache.put(policyImpl, rval);
                    }
                    handler.handle(AsyncResultImpl.create(rval));
                } else {
                    handler.handle(AsyncResultImpl.<IPolicy>create(new PolicyNotFoundException(policyImpl, result.getError())));
                }
            }
        });
    }

}
