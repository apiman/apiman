package io.apiman.manager.api.security.impl;

import io.apiman.common.config.options.GenericOptionsParser;
import io.apiman.common.config.options.Predicates;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class DiscoverabilityOptionsParser extends GenericOptionsParser {

    private final Map<String, RoleSourceDiscoverability> nameToDiscoverability = new HashMap<>();
    private final Multimap<Source, RoleSourceDiscoverability> sourceToDiscoverability = ArrayListMultimap.create();

    public DiscoverabilityOptionsParser(Map<String, String> options) {
        super(options);
        parseOptions();
    }

    private void parseOptions() {
        buildNameToDiscoverability();
        buildSourceToDiscoverability();
    }

    private void buildNameToDiscoverability() {
        getPrefix(options, 2).forEach((key, subMap) -> {
            nameToDiscoverability.put(key, new DisConfigParser(key, subMap).getRoleSourceDiscoverability());
        });
    }

    private void buildSourceToDiscoverability() {
        nameToDiscoverability.values().forEach(rsv -> {
            sourceToDiscoverability.put(rsv.getSource(), rsv);
        });
    }

    // TODO(msavy): consider pushing into GenericOptionsParser or AbstractOptions
    private Map<String, Map<String, String>> getPrefix(Map<String, String> options, int lim) {
        return options.entrySet()
                .stream()
                .map(e -> splitEntry(e, lim))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue,
                        (e1, e2) -> {
                            Map<String, String> hm = new HashMap<>(e1);
                            hm.putAll(e2);
                            return hm;
                        }
                ));
    }

    private Map.Entry<String, Map<String, String>> splitEntry(Entry<String, String> e, int lim) {
        String[] keySplit = e.getKey().split("\\.", lim);
        if (keySplit.length <= 1) {
            return null;
        } else {
            return Map.entry(keySplit[0], Map.of(keySplit[1], e.getValue()));
        }
    }

    public Multimap<Source, RoleSourceDiscoverability> getSourceToDiscoverability() {
        return sourceToDiscoverability;
    }

    public Map<String, RoleSourceDiscoverability> getNameToDiscoverability() {
        return nameToDiscoverability;
    }

    private static final class DisConfigParser extends GenericOptionsParser {
        private RoleSourceDiscoverability roleSourceDiscoverability;
        private final String name;

        public DisConfigParser(String name, Map<String, String> opts) {
            super(opts);
            this.name = name;
            parseConfig();
        }

        public String getName() {
            return name;
        }

        public RoleSourceDiscoverability getRoleSourceDiscoverability() {
            return roleSourceDiscoverability;
        }

        private void parseConfig() {
            Source source = getRequiredEnum(keys("source"), Source.class, Source::toValue);
            String discoverabilitiesJoined = getRequiredString(keys("discoverabilities"), Predicates.anyOk(), "");
            Set<DiscoverabilityLevel> discoverabilities = Arrays.stream(split(discoverabilitiesJoined, ','))
                    .map(DiscoverabilityLevel::toValue)
                    .collect(Collectors.toUnmodifiableSet());
            roleSourceDiscoverability = new RoleSourceDiscoverability(name, source, discoverabilities);
        }
    }

    // TODO(msavy): record candidate
    public static final class RoleSourceDiscoverability {
        String name;
        Source source;
        Set<DiscoverabilityLevel> discoverabilities;

        public RoleSourceDiscoverability(String name, Source source, Set<DiscoverabilityLevel> discoverabilities) {
            this.name = name;
            this.source = source;
            this.discoverabilities = discoverabilities;
        }

        public String getName() {
            return name;
        }

        public Source getSource() {
            return source;
        }

        public void setSource(Source source) {
            this.source = source;
        }

        public Set<DiscoverabilityLevel> getDiscoverabilities() {
            return discoverabilities;
        }

        public void setDiscoverabilities(Set<DiscoverabilityLevel> discoverabilities) {
            this.discoverabilities = discoverabilities;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RoleSourceDiscoverability that = (RoleSourceDiscoverability) o;
            return source == that.source && Objects.equals(discoverabilities, that.discoverabilities);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, discoverabilities);
        }
    }

    /**
     * Where information should be derived to determine discoverability of user
     */
    public enum Source {
        IDM_ROLE, IDM_ATTRIBUTE, APIMAN_ROLE, APIMAN_PERMISSION;

        public static Source toValue(String value) {
            return Enum.valueOf(Source.class, value.toUpperCase());
        }
    }
}
