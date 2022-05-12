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
package io.apiman.gateway.engine.policies;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.policies.config.IPListConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;

/**
 * Base class for the ip whitelist and blacklist policies.
 *
 * @author eric.wittmann@redhat.com
 * @param <C> the config type
 */
public abstract class AbstractIPListPolicy<C> extends AbstractMappedPolicy<C> {
    private final Map<IPListConfig, Set<IPAddressString>> ipPatternRulesCache = new HashMap<>();
    private final Cache<MatchCacheKey, Boolean> matchCache = Caffeine.newBuilder().maximumSize(10_000).build();
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(AbstractIPListPolicy.class);

    /**
     * Gets the remote address for comparison.
     * @param request the request
     * @param config the config
     */
    protected String getRemoteAddr(ApiRequest request, IPListConfig config) {
        String httpHeader = config.getHttpHeader();
        if (httpHeader != null && httpHeader.trim().length() > 0) {
            String value = request.getHeaders().get(httpHeader);
            if (value != null) {
                return value;
            }
        }
        return request.getRemoteAddr();
    }

    private Set<IPAddressString> getOrInstantiateRules(IPListConfig config) {
        Set<IPAddressString> patterns = ipPatternRulesCache.get(config);
        if (patterns != null) {
            return patterns;
        }
        patterns = config.getIpList().stream()
                .map(IPAddressString::new)
                .collect(Collectors.toSet());
        ipPatternRulesCache.put(config, patterns);
        return patterns;
    }

    /**
     * Returns true if the remote address is a match for the configured
     * values in the IP List.
     * @param config the config
     * @param remoteAddrStr the remote address
     */
    protected boolean isMatch(IPListConfig config, String remoteAddrStr) {
        try {
            return isMatchInternal(config, remoteAddrStr);
        } catch (AddressStringException e) {
            LOGGER.error(e, "Problem with matching rule: " + e.getMessage());
            // Emulate existing behaviour.
            return false;
        }
    }

    private boolean isMatchInternal(IPListConfig config, String remoteAddrStr) throws AddressStringException {
        // An exact match from the user-provided config. Shortcut.
        if (config.getIpList().contains(remoteAddrStr)) {
            return true;
        }
        // If we've matched this IP before, then we can just re-use that result.
        MatchCacheKey cacheKey = new MatchCacheKey(config, remoteAddrStr);
        Boolean cacheResult = matchCache.getIfPresent(cacheKey);
        if (cacheResult != null) {
            return cacheResult;
        }

        // Slow path, but we'll cache the result.
        Set<IPAddressString> rules = getOrInstantiateRules(config);
        for (IPAddressString rule : rules) {
            IPAddressString remoteAddr = new IPAddressString(remoteAddrStr);
            // If it's prefix like CIDR or a wildcard (/12, .*)
            if (rule.isPrefixed()) {
                if (rule.prefixEquals(remoteAddr)) {
                    cacheMatch(cacheKey, true);
                    return true;
                }
            } else {
                // Rule matches exactly (or via some direct match).
                if (rule.equals(remoteAddr) || rule.contains(remoteAddr) ) {
                    cacheMatch(cacheKey, true);
                    return true;
                }

                // If the rule is a range.
                String ruleAsString = rule.toString();
                boolean isRange = ruleAsString.contains("-");
                if (isRange) {
                    String[] rangeSplit = ruleAsString.split("-");
                    if (rangeSplit.length != 2) {
                        throw new IllegalArgumentException("Invalid range rule: " + ruleAsString);
                    }
                    IPAddress lower = new IPAddressString(rangeSplit[0]).toAddress();
                    IPAddress upper = new IPAddressString(rangeSplit[1]).toAddress();
                    IPAddressSeqRange ruleRange = lower.spanWithRange(upper);
                    cacheMatch(cacheKey, true);
                    return ruleRange.contains(remoteAddr.toAddress());
                }
            }
        }
        cacheMatch(cacheKey, false);
        return false;
    }

    private void cacheMatch(MatchCacheKey key, boolean result) {
        matchCache.put(key, result);
    }

    private static final class MatchCacheKey {

        private final IPListConfig config;
        private final String addr;

        public MatchCacheKey(IPListConfig config, String addr) {
            this.config = config;
            this.addr = addr;
        }

        public IPListConfig getConfig() {
            return config;
        }

        public String getAddr() {
            return addr;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MatchCacheKey that = (MatchCacheKey) o;
            return Objects.equals(config, that.config) && Objects.equals(addr, that.addr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(config, addr);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", MatchCacheKey.class.getSimpleName() + "[", "]")
                    .add("config=" + config)
                    .add("addr='" + addr + "'")
                    .toString();
        }
    }
}
