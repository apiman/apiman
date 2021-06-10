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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class GenericOptionsParserTest {

    @Test
    public void Given_OptionMap_When_GettingGettingKey_Then_ShouldFindValueCaseInsensitively() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "true");
        }};
        GenericOptionsParser opts = new GenericOptionsParser(map);
        assertThat(opts.getBool(Collections.singletonList("SEYCHELLES"), false)).isTrue();
    }

    // Boolean

    @Test
    public void Given_OptionMapWithNoMatchingKey_When_ParsingBooleanValue_Then_ShouldUseDefaultValue() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "unrelatedThing");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        boolean actual = opts.getBool(Collections.singletonList("mahe"), true);

        // Should come from defaultValue above
        assertThat(actual).isTrue();
    }

    @Test
    public void Given_OptionMapWithBooleans_When_ParsingValidTrueBoolean_Then_ValueShouldBeTrue() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "true");
            put("mahe", "yes");
            put("aldabra", "TRUE");
            put("coetivy", "y");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);

        assertThat(opts.getBool(Arrays.asList("skipMe", "seychelles"), false)).isTrue();
        assertThat(opts.getBool(Collections.singletonList("mahe"), false)).isTrue();
        assertThat(opts.getBool(Collections.singletonList("aldabra"), false)).isTrue();
        assertThat(opts.getBool(Arrays.asList("coetivy", "willbeignored"), false)).isTrue();
    }

    @Test
    public void Given_OptionMapWithBooleans_When_ParsingValidFalseBoolean_Then_ValueShouldBeFalse() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "false");
            put("mahe", "no");
            put("aldabra", "FALSE");
            put("coetivy", "n");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);

        assertThat(opts.getBool(Arrays.asList("skipMe", "seychelles"), true)).isFalse();
        assertThat(opts.getBool(Collections.singletonList("mahe"), true)).isFalse();
        assertThat(opts.getBool(Collections.singletonList("aldabra"), true)).isFalse();
        assertThat(opts.getBool(Arrays.asList("coetivy", "willbeignored"), true)).isFalse();
    }

    @Test(expected = BadOptionConfigurationException.class)
    public void Given_OptionWithInteger_When_ParsingAsBoolean_Then_ShouldThrowBadConfigurationException() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "8080");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        opts.getBool(Collections.singletonList("seychelles"), true);
    }

    // Integer

    @Test
    public void Given_OptionMapWithNoMatchingKey_When_ParsingIntegerValue_Then_ShouldUseDefaultValue() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "unrelatedThing");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        int actual = opts.getInt(Collections.singletonList("mahe"), 9001, f -> f > 0, "hello");

        // Should come from defaultValue above
        assertThat(actual).isEqualTo(9001);
    }

    @Test
    public void Given_OptionMapWithValidInteger_When_ParsingAsInteger_ThenShouldParseSuccessfully() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "8080");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        int actual = opts.getInt(Collections.singletonList("seychelles"), 5, v -> v > 0,
            "Value should be greater than zero");

        assertThat(actual).isEqualTo(8080);
    }

    @Test(expected = BadOptionConfigurationException.class)
    public void Given_OptionMapWithMalformattedInteger_When_ParsingAsInteger_Then_ShouldThrowBadConfigurationException() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "definitely not an integer");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        opts.getInt(Collections.singletonList("seychelles"), 5, v -> v > 0,
            "This will never be reached");
    }

    @Test(expected = BadOptionConfigurationException.class)
    public void Given_OptionMapWithInvalidInteger_When_ParsingAsInteger_Then_ShouldFailConstraintCheck() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "-100");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        opts.getInt(
            Collections.singletonList("seychelles"),
            5,
            // Note constraint check requires value greater than zero
            v -> v > 0,
            "This will never be reached"
        );
    }
    
    // Long

    @Test
    public void Given_OptionMapWithNoMatchingKey_When_ParsingLongValue_Then_ShouldUseDefaultValue() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "unrelatedThing");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        long actual = opts.getLong(Collections.singletonList("mahe"), 9001L, f -> f > 0, "hello");

        // Should come from defaultValue above
        assertThat(actual).isEqualTo(9001L);
    }

    @Test
    public void Given_OptionMapWithValidLong_When_ParsingAsLong_ThenShouldParseSuccessfully() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", Long.toString(Long.MAX_VALUE));
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        long actual = opts.getLong(Collections.singletonList("seychelles"), 5, v -> v > 0,
            "Value should be greater than zero");

        assertThat(actual).isEqualTo(Long.MAX_VALUE);
    }

    @Test(expected = BadOptionConfigurationException.class)
    public void Given_OptionMapWithMalformattedLong_When_ParsingAsLong_Then_ShouldThrowBadConfigurationException() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "definitely not an Long");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        opts.getLong(Collections.singletonList("seychelles"), 5, v -> v > 0,
            "This will never be reached");
    }

    @Test(expected = BadOptionConfigurationException.class)
    public void Given_OptionMapWithInvalidLong_When_ParsingAsLong_Then_ShouldFailConstraintCheck() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "-1");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        opts.getLong(
            Collections.singletonList("seychelles"),
            5,
            // Note constraint check requires value greater than zero
            v -> v > 0,
            "This will never be reached"
        );
    }
    
    // String

    @Test(expected = BadOptionConfigurationException.class)
    public void Given_OptionMapWithInvalidString_When_ParsingAsString_Then_ShouldFailConstraintCheck() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "this is too short");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        opts.getString(
            Collections.singletonList("seychelles"),
            "this value instead",
            // Note constraint check requires value greater than zero
            v -> v.length() > 100,
            "This will never be reached"
        );
    }

    @Test
    public void Given_OptionMapWithNoMatchingKey_When_ParsingStringValue_Then_ShouldUseDefaultValue() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "unrelatedThing");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        String actual = opts.getString(
            Collections.singletonList("mahe"),
            "praslin",
            s -> s.length() > 3,
            "black parrot says hello"
        );

        // Should come from defaultValue above
        assertThat(actual).isEqualTo("praslin");
    }


}