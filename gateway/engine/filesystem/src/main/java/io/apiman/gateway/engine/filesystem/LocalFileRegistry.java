package io.apiman.gateway.engine.filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.engine.filesystem.model.RegistryWrapper;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * Adds local file based registry implementation.
 *
 * @author Pete Cornish
 */
public class LocalFileRegistry extends InMemoryRegistry {
    static final String CONFIG_REGISTRY_PATH = "registry-path";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Object mutex = new Object();
    private final File registryFile;

    /**
     * The cached version of the registry.
     */
    private Map<String, Object> map;

    public LocalFileRegistry(Map<String, String> config) {
        final String registryPath = config.get(CONFIG_REGISTRY_PATH);
        if (StringUtils.isEmpty(registryPath)) {
            throw new IllegalStateException("Registry path is not set");
        } else {
            registryFile = new File(registryPath);
        }
    }

    /**
     * Read the {@link #map} from the filesystem.
     *
     * @return the registry map
     */
    @Override
    public Map<String, Object> getMap() {
        if (null == map) {
            synchronized (mutex) {
                // double-guard
                if (null == map) {
                    final Map<String, Object> registryMap = new ConcurrentHashMap<>();
                    if (registryFile.exists()) {
                        try (final InputStream in = FileUtils.openInputStream(registryFile)) {
                            final RegistryWrapper wrapper = mapper.readValue(in, RegistryWrapper.class);

                            registryMap.putAll(wrapper.getApis().stream()
                                    .collect(toMap(this::getApiIndex, identity())));
                            registryMap.putAll(wrapper.getClients().stream()
                                    .collect(toMap(this::getClientIndex, identity())));

                        } catch (Exception e) {
                            throw new RuntimeException("Error reading registry from file: " + registryFile, e);
                        }
                    }
                    map = registryMap;
                }
            }
        }
        return map;
    }

    /**
     * Store the {@link #map} to the filesystem.
     */
    private void persist() {
        synchronized (mutex) {
            try (final OutputStream out = FileUtils.openOutputStream(registryFile)) {
                final RegistryWrapper wrapper = new RegistryWrapper();
                wrapper.getApis().addAll(filterByType(map, Api.class));
                wrapper.getClients().addAll(filterByType(map, Client.class));

                mapper.writeValue(out, wrapper);

            } catch (Exception e) {
                throw new RuntimeException("Error persisting registry to file: " + registryFile, e);
            }
        }
    }

    /**
     * Clear the cached {@link #map}, forcing a reload from disk on next read.
     */
    void clear() {
        synchronized (mutex) {
            map = null;
        }
    }

    private <V> Collection<V> filterByType(Map<String, ?> entries, Class<V> clazz) {
        return entries.values().stream()
                .filter(e -> clazz.isAssignableFrom(e.getClass()))
                .map(clazz::cast)
                .collect(toSet());
    }

    @Override
    public void publishApi(Api api, IAsyncResultHandler<Void> handler) {
        super.publishApi(api, handler);
        persist();
    }

    @Override
    public void retireApi(Api api, IAsyncResultHandler<Void> handler) {
        super.retireApi(api, handler);
        persist();
    }

    @Override
    public void registerClient(Client client, IAsyncResultHandler<Void> handler) {
        super.registerClient(client, handler);
        persist();
    }

    @Override
    public void unregisterClient(Client client, IAsyncResultHandler<Void> handler) {
        super.unregisterClient(client, handler);
        persist();
    }

    @Override
    protected void unregisterClientInternal(Client client, boolean silent) throws RegistrationException {
        super.unregisterClientInternal(client, silent);
        persist();
    }
}
