/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.common.util;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.TreeMap;

import com.ibm.icu.util.LocaleMatcher;
import com.ibm.icu.util.LocaleMatcher.Builder;

/**
 * Base class for i18n messages classes.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class AbstractMessages {

    private static final List<String> FORMATS = Collections.singletonList("java.properties");
    private static final Map<String, ResourceBundle> bundles = new HashMap<>();
    private final Class<? extends AbstractMessages> clazz;
    private static final ThreadLocal<Locale> tlocale = new ThreadLocal<>();
    private final LocaleMatcher localeMatcher;

    /**
     * Set the message locale.
     */
    public static void setLocale(Locale locale) {
        tlocale.set(locale);
    }

    /**
     * Clear the message locale.
     */
    public static void clearLocale() {
        tlocale.set(null);
    }

    /**
     * Constructor.
     * @param c the class
     */
    public AbstractMessages(Class<? extends AbstractMessages> c) {
        clazz = c;
        // Find available bundle locales
        Builder lmBuilder = LocaleMatcher.builder();
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(clazz.getName(), locale);
                lmBuilder.addSupportedLocale(bundle.getLocale());
            } catch (MissingResourceException ignored) {}
        }
        lmBuilder.addSupportedLocale(Locale.ENGLISH)
                .setDefaultLocale(Locale.ENGLISH);
        this.localeMatcher = lmBuilder.build();
    }

    /**
     * Gets a bundle.  First tries to find one in the cache, then loads it if
     * it can't find one.
     */
    private ResourceBundle getBundle(String name) {
        String bundleKey = getBundleKey(name);
        if (bundles.containsKey(bundleKey)) {
            return bundles.get(bundleKey);
        } else {
            ResourceBundle bundle = loadBundle();
            bundles.put(bundleKey, bundle);
            return bundle;
        }
    }

    /**
     * Gets the key to use into the cache of bundles.  The key is made up of the
     * fully qualified class name and the locale.
     */
    private String getBundleKey(String name) {
        Locale bestLocale = localeMatcher.getBestLocale(getLocale());
        if (bestLocale != null) {
            return name + "::" + bestLocale;
        } else {
            return name + "::en";
        }
    }

    /**
     * Loads the resource bundle.
     */
    private ResourceBundle loadBundle() {
        String pkg = clazz.getPackage().getName();
        Locale locale = getLocale();
        return PropertyResourceBundle.getBundle(pkg + ".messages", locale, clazz.getClassLoader(), new ResourceBundle.Control() {
            @Override
            public List<String> getFormats(String baseName) {
                return FORMATS;
            }
        });
    }

    /**
     * Gets the locale to use when finding a bundle.  The locale to use is either from the
     * thread local value, if set, or else the system default locale.
     * @return the locale
     */
    public static Locale getLocale() {
        if (tlocale.get() != null) {
            return tlocale.get();
        } else {
            return Locale.getDefault();
        }
    }

    public static void addResourceBundle(String baseName, ResourceBundle resourceBundle) {
        Objects.requireNonNull(baseName);
        Objects.requireNonNull(resourceBundle);
        Locale locale = resourceBundle.getLocale();
        if (locale == null) {
            bundles.put(baseName + "::en", resourceBundle);
        } else {
            bundles.put(baseName + "::" + locale, resourceBundle);
        }
    }

    /**
     * Look up a message in the i18n resource message bundle by key, then format the
     * message with the given params and return the result.
     * @param key the key
     * @param params the parameters
     * @return formatted string
     */
    public String format(String key, Object ... params) {
        ResourceBundle bundle = getBundle(clazz.getName());
        if (bundle.containsKey(key)) {
            String msg = bundle.getString(key);
            return MessageFormat.format(msg, params);
        } else {
            return MessageFormat.format("!!{0}!!", key);
        }
    }
    
    /**
     * @return all strings in the bundle
     */
    public Map<String, String> all() {
        Map<String, String> rval = new TreeMap<>();
        ResourceBundle bundle = getBundle(clazz.getName());
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            rval.put(key, bundle.getString(key));
        }
        return rval;
    }

    public Map<String, String> get(String name) {
        Map<String, String> rval = new TreeMap<>();
        ResourceBundle bundle = getBundle(name);
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            rval.put(key, bundle.getString(key));
        }
        return rval;
    }

}
