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
package io.apiman.gateway.engine.impl;

import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginClassLoader;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.common.plugin.PluginSpec;
import io.apiman.common.plugin.PluginUtils;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.i18n.Messages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * A simple plugin registry that stores plugins in a temporary location.  This
 * implementation shouldn't really be used except for testing and perhaps getting
 * started with embedding the policy engine.  The reasons to not use this
 * implementation include:
 *
 * 1) not truly asynchronous (not good if embedding in a true async platform)
 * 2) stores downloaded plugins in java.io.tmp
 * 3) does not remember where it put plugins, so will re-download them often
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultPluginRegistry implements IPluginRegistry {
    private File pluginsDir;
    private final Map<PluginCoordinates, Plugin> pluginCache = new HashMap<>();
    private Map<PluginCoordinates, Throwable> errorCache = new HashMap<>();
    private Set<URI> pluginRepositories;

    /**
     * Constructor.
     */
    public DefaultPluginRegistry() {
        this(createTempPluginsDir(), PluginUtils.getDefaultMavenRepositories());
    }

    /**
     * Constructor.
     * @param configMap the configuration map
     */
    public DefaultPluginRegistry(Map<String, String> configMap) {
        this(getConfiguredPluginsDir(configMap), getConfiguredPluginRepositories(configMap));
    }

    /**
     * Constructor.
     * @param pluginsDir the plugins directory
     */
    public DefaultPluginRegistry(File pluginsDir) {
        this(pluginsDir, PluginUtils.getDefaultMavenRepositories());
    }

    /**
     * Constructor.
     * @param pluginsDir the plugins directory
     * @param pluginRepositories the plugin repositories
     */
    public DefaultPluginRegistry(File pluginsDir, Set<URI> pluginRepositories) {
        this.pluginsDir = pluginsDir;
        this.pluginRepositories = pluginRepositories;
    }

    private static final File createTempPluginsDir() {
        // TODO log a warning here
        try {
            @SuppressWarnings("nls")
            File tempDir = File.createTempFile("_apiman", "plugins");
            tempDir.delete();
            tempDir.mkdir();
            return tempDir;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getConfiguredPluginsDir(Map<String, String> configMap) {
        String pluginsDirPath = configMap.get("pluginsDir"); //$NON-NLS-1$
        if (pluginsDirPath != null) {
            File file = new File(pluginsDirPath).getAbsoluteFile();
            if (!file.exists()) {
                file.mkdirs();
            } else if (file.isFile()) {
                throw new RuntimeException("Invalid plugins directory: " + file.toString()); //$NON-NLS-1$
            }
            return file;
        } else {
            return createTempPluginsDir();
        }
    }
    private static Set<URI> getConfiguredPluginRepositories(Map<String, String> configMap) {
        Set<URI> rval = new HashSet<>();
        rval.addAll(PluginUtils.getDefaultMavenRepositories());
        String repositories = configMap.get("pluginRepositories"); //$NON-NLS-1$
        if (repositories != null) {
            String[] split = repositories.split(","); //$NON-NLS-1$
            for (String repository : split) {
                try {
                    String trimmedRepo = repository.trim();
                    if (!trimmedRepo.isEmpty()) {
                        if (trimmedRepo.startsWith("file:")) { //$NON-NLS-1$
                            trimmedRepo = trimmedRepo.replace('\\', '/');
                        }
                        rval.add(new URI(trimmedRepo));
                    }
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return rval;
    }

    /**
     * @see io.apiman.gateway.engine.IPluginRegistry#loadPlugin(io.apiman.common.plugin.PluginCoordinates, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public Future<IAsyncResult<Plugin>> loadPlugin(final PluginCoordinates coordinates, final IAsyncResultHandler<Plugin> userHandler) {
        final PluginFuture future = new PluginFuture();
        final boolean isSnapshot = PluginUtils.isSnapshot(coordinates);

        // Wrap the user provided handler so we can hook into the response.  We want to cache
        // the result (regardless of whether it's a success or failure)
        final IAsyncResultHandler<Plugin> handler = (IAsyncResult<Plugin> result) -> {
            synchronized (pluginCache) {
                if (result.isError()) {
                    errorCache.put(coordinates, result.getError());
                } else {
                    // Make sure we *always* use whatever is in the cache.  This resolves a
                    // race condition where multiple threads could ask for the plugin at the
                    // same time, resulting in two or more threads downloading the plugin.
                    // This is OK as long as we make sure we only ever use one.
                    if (pluginCache.containsKey(coordinates)) {
                        try { result.getResult().getLoader().close(); } catch (IOException e) {}
                        result = AsyncResultImpl.create(pluginCache.get(coordinates));
                    } else {
                        pluginCache.put(coordinates, result.getResult());
                    }
                }
            }
            if (userHandler != null) {
                userHandler.handle(result);
            }
            future.setResult(result);
        };

        boolean handled = false;
        synchronized (pluginCache) {

            // First check the cache.
            if (pluginCache.containsKey(coordinates)) {
                // Invoke the user handler directly - we know we don't need to re-cache it.
                AsyncResultImpl<Plugin> result = AsyncResultImpl.create(pluginCache.get(coordinates));
                if (userHandler != null) {
                    userHandler.handle(result);
                }
                future.setResult(result);
                handled = true;
            }

            // Check the error cache - don't keep trying again and again for a failure.
            if (!handled && errorCache.containsKey(coordinates)) {
                // Invoke the user handle directly - we know we don't need to re-cache it.
                AsyncResultImpl<Plugin> result = AsyncResultImpl.create(errorCache.get(coordinates), Plugin.class);
                if (userHandler != null) {
                    userHandler.handle(result);
                }
                future.setResult(result);
                handled = true;
            }
        }

        String pluginRelativePath = PluginUtils.getPluginRelativePath(coordinates);
        File pluginDir = new File(pluginsDir, pluginRelativePath);
        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }
        File pluginFile = new File(pluginDir, "plugin." + coordinates.getType()); //$NON-NLS-1$

        // Next try to load it from the plugin file registry
        if (!handled && pluginFile.isFile()) {
        	// If it's a snapshot, delete it here. (first time loading this plugin from the plugin file registry).  This
        	// means that snapshot plugins will be redownloaded each time the server is restarted.
            if (isSnapshot) {
                try { FileUtils.deleteDirectory(pluginDir); } catch (IOException e) { }
            } else {
                handled = true;
                try {
                    handler.handle(AsyncResultImpl.create(readPluginFile(coordinates, pluginFile)));
                } catch (Exception error) {
                    handler.handle(AsyncResultImpl.<Plugin>create(error));
                }
            }
        }

        // Next try to load it from the user's .m2 directory (copy it into the plugin file
        // registry first though)
        if (!handled) {
            File m2Dir = PluginUtils.getUserM2Repository();
            String m2Override = System.getProperty("apiman.gateway.m2-repository-path"); //$NON-NLS-1$
            if (m2Override != null) {
                m2Dir = new File(m2Override).getAbsoluteFile();
            }
            if (m2Dir != null) {
                File artifactFile = PluginUtils.getM2Path(m2Dir, coordinates);
                if (artifactFile.isFile()) {
                    handled = true;
                    try {
                        FileUtils.copyFile(artifactFile, pluginFile);
                        handler.handle(AsyncResultImpl.create(readPluginFile(coordinates, pluginFile)));
                    } catch (Exception error) {
                        handler.handle(AsyncResultImpl.<Plugin>create(error));
                    }
                }
            }
        }

        // Last effort - try to download it from a remote maven repository.  If this fails, then
        // we have to simply report "plugin not found".
        if (!handled) {
            downloadPlugin(coordinates, (IAsyncResult<File> result) -> {
                if (result.isSuccess()) {
                    File downloadedArtifactFile = result.getResult();
                    if (downloadedArtifactFile == null || !downloadedArtifactFile.isFile()) {
                        handler.handle(AsyncResultImpl.<Plugin>create(new Exception(Messages.i18n.format("DefaultPluginRegistry.PluginNotFound")))); //$NON-NLS-1$
                    } else {
                        try {
                            String pluginRelativePath1 = PluginUtils.getPluginRelativePath(coordinates);
                            File pluginDir1 = new File(pluginsDir, pluginRelativePath1);
                            if (!pluginDir1.exists()) {
                                pluginDir1.mkdirs();
                            }
                            File pluginFile1 = new File(pluginDir1, "plugin." + coordinates.getType()); //$NON-NLS-1$
                            if (!pluginFile1.exists()) {
                                FileUtils.copyFile(downloadedArtifactFile, pluginFile1);
                                FileUtils.deleteQuietly(downloadedArtifactFile);
                            } else {
                                FileUtils.deleteQuietly(downloadedArtifactFile);
                            }
                            handler.handle(AsyncResultImpl.create(readPluginFile(coordinates, pluginFile1)));
                        } catch (Exception error) {
                            handler.handle(AsyncResultImpl.<Plugin>create(error));
                        }
                    }
                } else {
                    handler.handle(AsyncResultImpl.<Plugin>create(result.getError()));
                }
            });
        }

        return future;
    }

    /**
     * Reads the plugin into an object.  This method will fail if the plugin is not valid.
     * This could happen if the file is not a java archive, or if the plugin spec file is
     * missing from the archive, etc.
     */
    protected Plugin readPluginFile(PluginCoordinates coordinates, File pluginFile) throws Exception {
        try {
            PluginClassLoader pluginClassLoader = createPluginClassLoader(pluginFile);
            URL specFile = pluginClassLoader.getResource(PluginUtils.PLUGIN_SPEC_PATH);
            if (specFile == null) {
                throw new Exception(Messages.i18n.format("DefaultPluginRegistry.MissingPluginSpecFile", PluginUtils.PLUGIN_SPEC_PATH)); //$NON-NLS-1$
            } else {
                PluginSpec spec = PluginUtils.readPluginSpecFile(specFile);
                Plugin plugin = new Plugin(spec, coordinates, pluginClassLoader);
                // TODO use logger when available
                System.out.println("Read apiman plugin: " + spec); //$NON-NLS-1$
                return plugin;
            }
        } catch (Exception e) {
            throw new Exception(Messages.i18n.format("DefaultPluginRegistry.InvalidPlugin", pluginFile.getAbsolutePath()), e); //$NON-NLS-1$
        }
    }

    /**
     * Creates a plugin classloader for the given plugin file.
     */
    protected PluginClassLoader createPluginClassLoader(final File pluginFile) throws IOException {
        return new PluginClassLoader(pluginFile, Thread.currentThread().getContextClassLoader()) {
            @Override
            protected File createWorkDir(File pluginArtifactFile) throws IOException {
                File workDir = new File(pluginFile.getParentFile(), ".work"); //$NON-NLS-1$
                workDir.mkdirs();
                return workDir;
            }
        };
    }

    /**
     * Downloads the plugin via its maven GAV information.  This will first look in the local
     * .m2 directory.  If the plugin is not found there, then it will try to download the
     * plugin from one of the configured remote maven repositories.
     */
    protected void downloadPlugin(final PluginCoordinates coordinates, final IAsyncResultHandler<File> handler) {
        if (pluginRepositories.isEmpty()) {
            // Didn't find it - no repositories configured!
            handler.handle(AsyncResultImpl.create((File) null));
            return;
        }

        final Iterator<URI> iterator = pluginRepositories.iterator();
        URI repoUrl = iterator.next();
        final IAsyncResultHandler<File> handler2 = new IAsyncResultHandler<File>() {
            @Override
            public void handle(IAsyncResult<File> result) {
                if (result.isSuccess() && result.getResult() == null && iterator.hasNext()) {
                    downloadFromMavenRepo(coordinates, iterator.next(), this);
                } else {
                    handler.handle(result);
                }
            }
        };
        downloadFromMavenRepo(coordinates, repoUrl, handler2);
    }

    /**
     * Tries to download the plugin from the given remote maven repository.
     */
    protected void downloadFromMavenRepo(PluginCoordinates coordinates, URI mavenRepoUrl, IAsyncResultHandler<File> handler) {
        String artifactSubPath = PluginUtils.getMavenPath(coordinates);
        try {
            File tempArtifactFile = File.createTempFile("_plugin", "dwn"); //$NON-NLS-1$ //$NON-NLS-2$
            URL artifactUrl = new URL(mavenRepoUrl.toURL(), artifactSubPath);
            downloadArtifactTo(artifactUrl, tempArtifactFile, handler);
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.<File>create(e));
        }
    }

    /**
     * Download the artifact at the given URL and store it locally into the given
     * plugin file path.
     */
    protected void downloadArtifactTo(URL artifactUrl, File pluginFile, IAsyncResultHandler<File> handler) {
        InputStream istream = null;
        OutputStream ostream = null;
        try {
            URLConnection connection = artifactUrl.openConnection();
            connection.connect();
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                if (httpConnection.getResponseCode() != 200) {
                    handler.handle(AsyncResultImpl.create(null));
                    return;
                }
            }
            istream = connection.getInputStream();
            ostream = new FileOutputStream(pluginFile);
            IOUtils.copy(istream, ostream);
            ostream.flush();
            handler.handle(AsyncResultImpl.create(pluginFile));
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.<File>create(e));
        } finally {
            IOUtils.closeQuietly(istream);
            IOUtils.closeQuietly(ostream);
        }
    }

    private static final class PluginFuture implements Future<IAsyncResult<Plugin>> {

        private IAsyncResult<Plugin> result;
        private CountDownLatch latch = new CountDownLatch(1);

        /**
         * Constructor.
         */
        public PluginFuture() {
        }

        /**
         * @see java.util.concurrent.Future#cancel(boolean)
         */
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        /**
         * @see java.util.concurrent.Future#isCancelled()
         */
        @Override
        public boolean isCancelled() {
            return false;
        }

        /**
         * @see java.util.concurrent.Future#isDone()
         */
        @Override
        public boolean isDone() {
            return result != null;
        }

        /**
         * @see java.util.concurrent.Future#get()
         */
        @Override
        public IAsyncResult<Plugin> get() throws InterruptedException, ExecutionException {
            latch.await();
            return result;
        }

        /**
         * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
         */
        @Override
        public IAsyncResult<Plugin> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
                TimeoutException {
            if (latch.await(timeout, unit)) {
                return result;
            } else {
                throw new TimeoutException();
            }
        }

        /**
         * @param result the result to set
         */
        public void setResult(IAsyncResult<Plugin> result) {
            this.result = result;
            latch.countDown();
        }

    }

}
