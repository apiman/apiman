package io.apiman.manager.sso.keycloak;

import io.apiman.common.config.options.GenericOptionsParser;
import io.apiman.manager.sso.keycloak.approval.AccountApprovalOptions;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.keycloak.Config.Scope;

/**
 * Apiman's option parsing code works with a map interface, however, Keycloak's {@link Scope} (Config), does
 * not implement Map (only basic get operations). To work around this we provide a shim/delegate with the
 * majority of the methods stubbed out. It's not beautiful, but it works.
 *
 * @see GenericOptionsParser
 * @see AccountApprovalOptions
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class KeycloakOptsMapShim extends TreeMap<String, String> {

    private final Scope scope;

    public KeycloakOptsMapShim(Scope scope) {
        this.scope = scope;
    }

    @Override
    public int size() {
        return 1;
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
    public String get(@NotNull Object key) {
        if (!(key instanceof String)) {
            throw new IllegalArgumentException("Key must be String");
        }
        return scope.get((String) key);
    }

    @Nullable
    @Override
    public String put(@NotNull String key, String value) {
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
