package io.apiman.gateway.engine.filesystem;

import io.apiman.gateway.engine.beans.Api;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.apiman.gateway.engine.filesystem.LocalFileRegistry.CONFIG_REGISTRY_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Pete Cornish
 */
public class LocalFileRegistryTest {
    @Test
    public void readWriteFileEmptyRegistry() throws Exception {
        final File registryFile = Files.createTempFile("file-registry", ".json").toFile();

        // don't start with a saved registry - just use the filename
        //noinspection ResultOfMethodCallIgnored
        registryFile.delete();

        final Map<String, String> config = new HashMap<String, String>() {{
            put(CONFIG_REGISTRY_PATH, registryFile.getAbsolutePath());
        }};

        final LocalFileRegistry registry = new LocalFileRegistry(config);
        assertTrue("The map should be empty", registry.getMap().isEmpty());

        final Api api = new Api() {{
            setApiId("apiA");
            setApiPolicies(Collections.emptyList());
            setEndpoint("http://example.com");
            setEndpointType("REST");
            setOrganizationId("org");
            setPublicAPI(true);
            setVersion("1.0");
        }};

        registry.publishApi(api, result -> {
            assertTrue("Publish should be successful", result.isSuccess());
            assertFalse("The map should not be empty", registry.getMap().isEmpty());
        });

        // clear and re-read from file
        registry.clear();

        registry.getApi("org", "apiA", "1.0", result -> {
            assertTrue(result.isSuccess());
            assertNotNull(result.getResult());
            assertEquals("apiA", result.getResult().getApiId());
        });
    }

    @Test
    public void readExistingRegistry() {
        final Map<String, String> config = new HashMap<String, String>() {{
            put(CONFIG_REGISTRY_PATH, LocalFileRegistryTest.class.getResource("/populated-registry.json").getPath());
        }};

        final LocalFileRegistry registry = new LocalFileRegistry(config);

        assertFalse("The map should not be empty", registry.getMap().isEmpty());
        assertNotNull("The API should exist in the map", registry.getMap().get("API::org|apiB|1.0"));

        registry.getApi("org", "apiB", "1.0", result -> {
            assertTrue(result.isSuccess());
            assertNotNull(result.getResult());
            assertEquals("apiB", result.getResult().getApiId());
        });
    }
}
