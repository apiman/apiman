package io.apiman.manager.sso.keycloak;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.keycloak.Config.Scope;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class KeycloakOptionsMapShim implements Map<String, String> {

    private final Scope scope;

    public KeycloakOptionsMapShim(Scope scope) {
        this.scope = scope;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
            throw new IllegalArgumentException("Key must be String");
        }
        return scope.get((String) key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Can't test for values");
    }

    @Override
    public String get(Object key) {
        if (!(key instanceof String)) {
            throw new IllegalArgumentException("Key must be String");
        }
        return scope.get((String) key);
    }

    @Nullable
    @Override
    public String put(String key, String value) {
        throw new UnsupportedOperationException("Read only configuration data");
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException("Read only configuration data");
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException("Read only configuration data");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Read only configuration data");
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException("No access to key set");
    }

    @NotNull
    @Override
    public Collection<String> values() {
        throw new UnsupportedOperationException("No access to value set");
    }

    @NotNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        throw new UnsupportedOperationException("No access to entry set");
    }
}
