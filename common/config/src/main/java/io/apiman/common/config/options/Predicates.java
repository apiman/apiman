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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

/**
 * Simple predicates for common option parsing use-cases. Typically used for constraint checking.
 *
 * <p>Note that the methods present in {@link Predicate} can be used to chain these functions together,
 * which can be useful for composition. For example, {@code noWhitespace().and(s -> s.contains("foo")) }
 *
 * @see GenericOptionsParser
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class Predicates {

    public static <T> Predicate<T> anyOk() {
        return any -> true;
    }

    public static String noWhitespaceMsg() {
        return "must not contain any whitespace";
    }

    public static Predicate<String> noWhitespace() {
        return s -> !StringUtils.containsWhitespace(s);
    }

    public static String matchesAnyMsg(String... allowedValues) {
        return "must be one of: " + String.join(", ", allowedValues);
    }

    public static Predicate<String> matchesAny(String... allowedValues) {
        return input -> Arrays.stream(allowedValues)
            .map(StringUtils::strip)
            .anyMatch(allowed -> allowed.equalsIgnoreCase(input));
    }

    public static String greaterThanZeroMsg() {
        return "must be greater than zero";
    }

    public static Predicate<Long> greaterThanZeroLong() {
        return input -> input > 0;
    }

    public static Predicate<Integer> greaterThanZeroInt() {
        return input -> input > 0;
    }

    public static String fileExistsMsg(String description) {
        return description + " file was not found";
    }

    public static Predicate<String> fileExists() {
        return p -> Files.exists(Paths.get(p));
    }
}
