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

    public Messages(String bundleName, String messagePrefix) {
        this.messagePrefix = (StringUtils.isNotBlank(messagePrefix) ? messagePrefix + "." : "");
        this.resourceBundle = ResourceBundle.getBundle(bundleName);
    }

    /**
     * Creates a new {@link Messages} using the package name of the class as the path and the simple name of the
     * class as the message prefix.
     *
     * @param clazz the Class for which to get the message bundle
     */
    public Messages(Class<?> clazz) {
        this(clazz.getPackage().getName() + "." + DEFAULT_BUNDLE_NAME, clazz.getSimpleName());
    }

    /**
     * Creates a new {@link Messages} using the package name of the class as the path and the simple name of the
     * class as the message prefix.
     *
     * @param clazz the Class for which to get the message bundle
     * @return a new message bundle
     */
    public static Messages getMessageBundle(Class<?> clazz) {
        return new Messages(clazz);
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
