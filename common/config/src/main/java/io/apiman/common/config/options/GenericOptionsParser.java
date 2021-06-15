/*
 * Copyright 2021 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apiman.common.config.options;

import io.apiman.common.config.options.exceptions.BadOptionConfigurationException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * A generic options parser for {@code Map<String, String> options}, which is commonly found on components,
 * etc.
 *
 * <p>If a component has relatively simple options parsing requirements, then this parser may suffice,
 * rather than writing your own {@link AbstractOptions} implementation in a fairly robust way.
 *
 * <p>Broadly, it has the following features:
 *
 * <dl>
 *     <dt>Key aliases</dt>
 *     <dd>Ordered list of key aliases to look up, the first non-null value will be returned.</dd>
 *
 *     <dt>Key insensitivity</dt>
 *     <dd>Key matches are cAsE iNSenSiTiVe.</dd>
 *
 *     <dt>Default value</dt>
 *     <dd>If no non-null value is found via any of the aliases, then a default value will be returned.</dd>
 *
 *     <dt>Constraint checking</dt>
 *     <dd>A boolean constraint check invoked on the first value that is successfully parsed.
 *     A {@link BadOptionConfigurationException} will be thrown that describes the failure
 *     using information provided, plus a provided message.
 *     </dd>
 *
 *     <dt>Constraint violation message</dt>
 *     <dd>A useful message appended to any generated exception when a constraint violation occurs.
 *     The implementor should use it to describe why a constraint failure triggered. For example,
 *     'port must be greater than zero'.
 *     </dd>
 * </dl>
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}.
 */
public class GenericOptionsParser extends AbstractOptions {

    private TreeMap<String, String> options;

    public GenericOptionsParser(Map<String, String> options) {
        super(options);
    }

    @Override
    protected void parse(Map<String, String> options) {
        this.options = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.options.putAll(options);
    }

    protected TreeMap<String, String> getOptions() {
        return options;
    }

    /**
     * Parse a boolean from the options using the key aliases.
     *
     * @param keyAliases   the key aliases
     * @param defaultValue the default value
     * @return the parsed boolean, or the default value if none found
     * @throws BadOptionConfigurationException if a non-empty value corresponding to a key is found that is
     *                                         not recognised as a boolean (e.g. foo is not obviously
     *                                         true/false, so an exception will be thrown).
     */
    public boolean getBool(List<String> keyAliases, Boolean defaultValue) {
        AliasValueEntry candidate = getValue(keyAliases);
        if (candidate == null || StringUtils.isBlank(candidate.value)) {
            return defaultValue;
        }
        // If a value was provided, but the boolean converter returned null, then the value was
        // an unparseable (i.e. an invalid format). Throw an exception to indicate this.
        Boolean parsed = BooleanUtils.toBooleanObject(candidate.value);
        if (parsed == null) {
            throw BadOptionConfigurationException.parseFailure(candidate.alias, "boolean", candidate.value);
        }
        return parsed;
    }

    /**
     * Parse an integer from the options using the key aliases.
     *
     * @param keyAliases   the key aliases
     * @param defaultValue the default value
     * @param message      a human-readable message to describe the constraint and display in the case of a
     *                     constraint violation
     * @return the parsed integer, or the default value if none found
     * @throws BadOptionConfigurationException if a value is not a valid integer or a constraint violation
     *                                         occurs (e.g. X > 5).
     */
    public int getInt(List<String> keyAliases, int defaultValue, Predicate<Integer> constraint,
        String message) {

        AliasValueEntry candidate = getValue(keyAliases);
        if (candidate == null || StringUtils.isBlank(candidate.value)) {
            return defaultValue;
        }
        try {
            int parsedInt = Integer.parseInt(candidate.value);
            if (!constraint.test(parsedInt)) {
                throw BadOptionConfigurationException
                    .constraintFailure(candidate.alias, "integer", candidate.value, message);
            }
            return parsedInt;
        } catch (NumberFormatException nfe) {
            throw BadOptionConfigurationException
                .parseFailure(candidate.alias, "integer", candidate.value, nfe);
        }
    }

    /**
     * Parse an integer from the options using the key aliases.
     *
     * @param keyAliases   the key aliases
     * @param defaultValue the default value
     * @param constraint   the constraint that the value should obey
     * @param message      a human-readable message to describe the constraint and display in the case of a
     *                     constraint violation.
     * @return the parsed long, or the default value if none found
     * @throws BadOptionConfigurationException if a value is not a valid long, or a constraint violation
     *                                         occurs (e.g. {@code X > 5}).
     */
    public long getLong(List<String> keyAliases, long defaultValue, Predicate<Long> constraint,
        String message) {

        AliasValueEntry candidate = getValue(keyAliases);
        if (candidate == null || StringUtils.isBlank(candidate.value)) {
            return defaultValue;
        }
        try {
            long parsedInt = Long.parseLong(candidate.value);
            if (!constraint.test(parsedInt)) {
                throw BadOptionConfigurationException
                    .constraintFailure(candidate.alias, "long", candidate.value, message);
            }
            return parsedInt;
        } catch (NumberFormatException nfe) {
            throw BadOptionConfigurationException.parseFailure(candidate.alias, "long", candidate.value, nfe);
        }
    }

    /**
     * As {@link #getString(List, String, Predicate, String)}, but additionally throw an exception if no value
     * is provided.
     *
     * @throws BadOptionConfigurationException if a constraint violation occurs or if no value was provided.
     * @see #getString(List, String, Predicate, String)
     */
    public String getRequiredString(List<String> keyAliases, Predicate<String> constraint, String message) {
        return Optional
            .ofNullable(getString(keyAliases, null, constraint, message))
            .orElseThrow(() -> BadOptionConfigurationException.requiredValue(keyAliases, "string"));
    }

    /**
     * Parse a string from the options using the key aliases.
     *
     * @param keyAliases   the key aliases
     * @param defaultValue the default value
     * @param constraint   the constraint that the value should obey
     * @param message      a human-readable message to describe the constraint and display in the case of a
     *                     constraint violation.
     * @return the parsed long, or the default value if none found
     * @throws BadOptionConfigurationException if a constraint violation occurs (e.g. {@code
     *                                         X.startsWith('foo')}).
     */
    public String getString(List<String> keyAliases, String defaultValue,
        Predicate<String> constraint, String message) {

        AliasValueEntry candidate = getValue(keyAliases);
        if (candidate == null || StringUtils.isBlank(candidate.value)) {
            return defaultValue;
        }
        if (!constraint.test(candidate.value)) {
            throw BadOptionConfigurationException
                .constraintFailure(candidate.alias, "string", candidate.value, message);
        }
        return candidate.value;
    }

    /**
     * As {@link #getPath(List, Path, Predicate, String)}, but additionally throw an exception if no value is
     * provided.
     *
     * @throws BadOptionConfigurationException if a constraint violation occurs or if no value was provided.
     * @see #getString(List, String, Predicate, String)
     */
    public Path getRequiredPath(List<String> keyAliases, Predicate<Path> constraint, String message) {
        return Optional
            .ofNullable(getPath(keyAliases, null, constraint, message))
            .orElseThrow(() -> BadOptionConfigurationException.requiredValue(keyAliases, "path"));
    }

    /**
     * Parse a {@link Path} from the options using the key aliases. Note that this method does not guarantee
     * that the file/path exists. Use a predicate for this such as {@link Predicates#fileExists()}.
     *
     * @param keyAliases   the key aliases
     * @param defaultValue the default value
     * @param constraint   the constraint that the value should obey
     * @param message      a human-readable message to describe the constraint and display in the case of a
     *                     constraint violation.
     * @return the parsed path
     * @throws BadOptionConfigurationException if a constraint violation occurs.
     * @see Predicates#fileExists() File exists predicate.
     */
    public Path getPath(List<String> keyAliases, Path defaultValue, Predicate<Path> constraint, String message) {
        AliasValueEntry candidate = getValue(keyAliases);
        if (candidate == null || StringUtils.isBlank(candidate.value)) {
            return defaultValue;
        }

        Path parsedPath = Paths.get(candidate.value);
        if (!constraint.test(parsedPath)) {
            throw BadOptionConfigurationException
                .constraintFailure(candidate.alias, "path", candidate.value, message);
        }
        return parsedPath;
    }

    /**
     * Varargs of key aliases into list for use with most get/parse methods.
     */
    @SafeVarargs
    public static <T> List<T> keys(T... keys) {
        return Arrays.asList(keys);
    }

    private AliasValueEntry getValue(List<String> keyAliases) {
        return keyAliases.stream()
            .filter(candidate -> options.containsKey(candidate))
            .map(candidate -> AliasValueEntry.of(candidate, StringUtils.strip(options.get(candidate))))
            .findFirst()
            .orElse(null);
    }

    private static final class AliasValueEntry {

        private final String alias;
        private final String value;

        AliasValueEntry(String alias, String value) {
            this.alias = alias;
            this.value = value;
        }

        static AliasValueEntry of(String alias, String value) {
            return new AliasValueEntry(alias, value);
        }
    }
}
