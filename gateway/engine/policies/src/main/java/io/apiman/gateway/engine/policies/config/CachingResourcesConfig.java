package io.apiman.gateway.engine.policies.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration object for the Caching policy.
 *
 * @author benjamin.kihm@scheer-group.com
 */
public class CachingResourcesConfig {

    private long ttl; // in seconds
    private List<CachingResourcesSettingsEntry> cachingResourcesSettingsEntries = new ArrayList<>();

    /**
     * Constructor.
     */
    public CachingResourcesConfig() {
    }

    /**
     * @return ttl the time to live
     */
    public long getTtl() {
        return ttl;
    }

    /**
     * @param ttl the time to live to set
     */
    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    /**
     * Contains the caching settings of the policy
     * @return getCachingResourcesSettingsEntries caching settings entries
     */
    public List<CachingResourcesSettingsEntry> getCachingResourcesSettingsEntries() {
        return cachingResourcesSettingsEntries;
    }

    /**
     * Sets the caching settings of the policy
     * @param cachingResourcesSettingsEntries caching settings entries to set
     */
    public void setCachingResourcesSettingsEntries(List<CachingResourcesSettingsEntry> cachingResourcesSettingsEntries) {
        this.cachingResourcesSettingsEntries = cachingResourcesSettingsEntries;
    }

}
