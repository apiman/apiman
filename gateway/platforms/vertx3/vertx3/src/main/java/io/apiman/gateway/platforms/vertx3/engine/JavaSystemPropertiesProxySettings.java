package io.apiman.gateway.platforms.vertx3.engine;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import io.netty.util.internal.SystemPropertyUtil;

public class JavaSystemPropertiesProxies extends HttpProxySettings.HttpProxy {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(JavaSystemPropertiesProxies.class);
    private final List<String> nonProxyHosts = new ArrayList<>();
    private final String propertyPrefix; // Allows non-standard ones like "https.proxyHost, etc"
    private Pattern noProxyRegex;

    public JavaSystemPropertiesProxies(String propertyPrefix, int defaultPort) {
        super(
            SystemPropertyUtil.get(propertyPrefix + ".proxyHost"),
            SystemPropertyUtil.getInt(propertyPrefix + ".proxyPort", defaultPort),
            SystemPropertyUtil.get(propertyPrefix + ".proxyUser"),
            SystemPropertyUtil.get(propertyPrefix + ".proxyPassword")
        );
        this.propertyPrefix = propertyPrefix;
        parseProxyHosts(SystemPropertyUtil.get(propertyPrefix + ".nonProxyHosts"));

        LOGGER.info("Proxy configuration found in System properties prefixed {0}:"
            + "Proxy host: {1}"
            + "Proxy port: {2}"
            + "Proxy credentials: {3}"
            + "Non-proxy hosts: {4}",
            propertyPrefix, super.getHost(), this.getPort(), this.getCredentials(), this.nonProxyHosts);
    }

    /**
     * Seems it is common to support wildcards at only the start or end of the entry, so we'll do that for
     * now. If anyone wants wildcard support elsewhere they can ask for it.
     *
     * We use Pattern.quote to make the rest of the string 'safe'.
     */
    private void parseProxyHosts(String noProxyList) {
        StringJoiner pattern = new StringJoiner("|");
        String[] split = noProxyList.split("\\|");
        nonProxyHosts.addAll(Arrays.asList(split));
        for (String noProxyElement : split) {
            // To match Java regex style we need to add any/`.` to `*` (i.e. .*)
            if (noProxyElement.endsWith("*")) {
                // Snip off * at end, replace with .*
                String toQuote = noProxyElement.substring(0, noProxyElement.length() - 1);
                pattern.add(Pattern.quote(toQuote) + ".*");
            } else if (noProxyElement.startsWith("*")) {
                // Snip off * at start, replace with .*
                String toQuote = noProxyElement.substring(1);
                pattern.add(".*" + Pattern.quote(toQuote));
            } else {
                pattern.add(Pattern.quote(noProxyElement));
            }
        }
        this.noProxyRegex = Pattern.compile(pattern.toString());
        LOGGER.debug("Pattern built for noProxy: {0}", noProxyRegex);
    }

    /**
     * Determine whether the given host should not be proxied.
     *
     * @param host hostname to test
     * @return true if is a `non proxy` host
     */
    public boolean isNonProxyHost(String host) {
        return noProxyRegex.matcher(host).matches();
    }
}
