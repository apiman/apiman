package io.apiman.gateway.engine.hazelcast;

import com.hazelcast.config.Config;
import io.apiman.gateway.engine.hazelcast.config.HazelcastInstanceManager;
import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.*;

/**
 * Tests for {@link HazelcastSharedStateComponent}.

 * @author Pete Cornish
 */
public class HazelcastSharedStateComponentTest {
    private HazelcastSharedStateComponent component;

    @Before
    public void setUp() throws Exception {
        final Config config = HazelcastConfigUtil.buildConfigWithDisabledNetwork();
        HazelcastInstanceManager.DEFAULT_MANAGER.setOverrideConfig(config);

        component = new HazelcastSharedStateComponent(emptyMap());
    }

    @Test
    public void getSetAndClearProperty() throws Exception {
        final String namespace = "namespace";
        final String propertyName = "propertyName";
        final String propertyValue = "propertyValue";

        component.setProperty(namespace, propertyName, propertyValue, result -> {
            assertFalse("The property should be set successfully", result.isError());
        });

        component.getProperty(namespace, propertyName, null, result -> {
            assertFalse("The property should be fetched successfully", result.isError());
            assertEquals(propertyValue, result.getResult());
        });

        component.clearProperty(namespace, propertyName, result -> {
            assertFalse("The property should be cleared successfully", result.isError());
        });

        // retrieve the default for a non-existent property
        component.getProperty(namespace, propertyName, "defaultValue", result -> {
            assertFalse("The default property value should be returned", result.isError());
            assertEquals("defaultValue", result.getResult());
        });
    }
}
