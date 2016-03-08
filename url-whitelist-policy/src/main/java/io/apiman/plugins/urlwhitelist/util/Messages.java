package io.apiman.plugins.urlwhitelist.util;

import org.apache.commons.lang.StringUtils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Formats messages from a ResourceBundle.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class Messages {
    private static final String DEFAULT_BUNDLE_NAME = "messages";

    private final ResourceBundle resourceBundle;
    private final String messagePrefix;

    /**
     * Creates a new {@link Messages} using the package name of the class as the path and the simple name of the
     * class as the message prefix.
     *
     * @param packageName the package name of the bundle
     * @param messagePrefix the message prefix (can be {@code null})
     */
    public Messages(String packageName, String messagePrefix) {
        this.messagePrefix = (StringUtils.isNotBlank(messagePrefix) ? messagePrefix + "." : "");
        this.resourceBundle = ResourceBundle.getBundle(packageName + "." + DEFAULT_BUNDLE_NAME);
    }

    /**
     * Return the message <code>key</code> formatted with the given <code>params</code>.
     *
     * @param key    the message key in the ResourceBundle
     * @param params the format arguments
     * @return the formatted String
     */
    public String format(String key, Object... params) {
        try {
            return String.format(resourceBundle.getString(messagePrefix + key), params);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
