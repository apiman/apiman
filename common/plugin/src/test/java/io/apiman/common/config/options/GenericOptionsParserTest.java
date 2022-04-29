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

import io.apiman.common.config.options.exceptions.InvalidOptionConfigurationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static io.apiman.common.config.options.GenericOptionsParser.keys;
import static org.assertj.core.api.Assertions.assertThat;

public class GenericOptionsParserTest {

    @Test
    public void Given_OptionMap_When_GettingKey_Then_ShouldFindValueCaseInsensitively() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "true");
        }};
        GenericOptionsParser opts = new GenericOptionsParser(map);
        assertThat(opts.getBool(keys("SEYCHELLES"), false)).isTrue();
    }

    // Boolean

    @Test
    public void Given_OptionMapWithNoMatchingKey_When_ParsingBooleanValue_Then_ShouldUseDefaultValue() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "unrelatedThing");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        boolean actual = opts.getBool(keys("mahe"), true);

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

        assertThat(opts.getBool(keys("skipMe", "seychelles"), false)).isTrue();
        assertThat(opts.getBool(keys("mahe"), false)).isTrue();
        assertThat(opts.getBool(keys("aldabra"), false)).isTrue();
        assertThat(opts.getBool(keys("coetivy", "willbeignored"), false)).isTrue();
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

        assertThat(opts.getBool(keys("skipMe", "seychelles"), true)).isFalse();
        assertThat(opts.getBool(keys("mahe"), true)).isFalse();
        assertThat(opts.getBool(keys("aldabra"), true)).isFalse();
        assertThat(opts.getBool(keys("coetivy", "willbeignored"), true)).isFalse();
    }

    @Test(expected = InvalidOptionConfigurationException.class)
    public void Given_OptionWithInteger_When_ParsingAsBoolean_Then_ShouldThrowBadConfigurationException() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "8080");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        opts.getBool(keys("seychelles"), true);
    }

    // Integer

    @Test
    public void Given_OptionMapWithNoMatchingKey_When_ParsingIntegerValue_Then_ShouldUseDefaultValue() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "unrelatedThing");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        int actual = opts.getInt(keys("mahe"), 9001, f -> f > 0, "hello");

        // Should come from defaultValue above
        assertThat(actual).isEqualTo(9001);
    }

    @Test
    public void Given_OptionMapWithValidInteger_When_ParsingAsInteger_ThenShouldParseSuccessfully() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "8080");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        int actual = opts.getInt(keys("seychelles"), 5, v -> v > 0,
            "Value should be greater than zero");

        assertThat(actual).isEqualTo(8080);
    }

    @Test(expected = InvalidOptionConfigurationException.class)
    public void Given_OptionMapWithMalformedInteger_When_ParsingAsInteger_Then_ShouldThrowBadConfigurationException() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "definitely not an integer");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        opts.getInt(keys("seychelles"), 5, v -> v > 0,
            "This will never be reached");
    }

    @Test(expected = InvalidOptionConfigurationException.class)
    public void Given_OptionMapWithInvalidInteger_When_ParsingAsInteger_Then_ShouldFailConstraintCheck() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "-100");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        opts.getInt(
            keys("seychelles"),
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
        long actual = opts.getLong(keys("mahe"), 9001L, f -> f > 0, "hello");

        // Should come from defaultValue above
        assertThat(actual).isEqualTo(9001L);
    }

    @Test
    public void Given_OptionMapWithValidLong_When_ParsingAsLong_ThenShouldParseSuccessfully() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", Long.toString(Long.MAX_VALUE));
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        long actual = opts.getLong(keys("seychelles"), 5, v -> v > 0,
            "Value should be greater than zero");

        assertThat(actual).isEqualTo(Long.MAX_VALUE);
    }

    @Test(expected = InvalidOptionConfigurationException.class)
    public void Given_OptionMapWithMalformedLong_When_ParsingAsLong_Then_ShouldThrowBadConfigurationException() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "definitely not an Long");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        opts.getLong(keys("seychelles"), 5, v -> v > 0,
            "This will never be reached");
    }

    @Test(expected = InvalidOptionConfigurationException.class)
    public void Given_OptionMapWithInvalidLong_When_ParsingAsLong_Then_ShouldFailConstraintCheck() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "-1");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        opts.getLong(
            keys("seychelles"),
            5,
            // Note constraint check requires value greater than zero
            v -> v > 0,
            "must be greater than zero"
        );
    }
    
    // String

    @Test(expected = InvalidOptionConfigurationException.class)
    public void Given_OptionMapWithInvalidString_When_ParsingAsString_Then_ShouldFailConstraintCheck() {
        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", "this is too short");
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        opts.getString(
            keys("seychelles"),
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
            keys("mahe"),
            "praslin",
            s -> s.length() > 3,
            "black parrot says hello"
        );

        // Should come from defaultValue above
        assertThat(actual).isEqualTo("praslin");
    }

    // Path

    @Test
    public void Given_OptionMapWithValidPath_When_ParsingPath_Then_ShouldReturnParsedPath()
        throws IOException {
        Path tempFile = Files.createTempFile("apiman-test", "bar");
        Files.write(tempFile, "some ole nonsense".getBytes(StandardCharsets.UTF_8));

        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", tempFile.toAbsolutePath().toString());
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);
        Path parsedPath = opts.getPath(
            keys("seychelles"),
            null,
            Predicates.fileExists().and(Predicates.fileSizeGreaterThanZero()),
            "File must exist and be greater then zero in size"
        );

        assertThat(parsedPath).exists();
        assertThat(parsedPath).hasContent("some ole nonsense");
    }

    @Test(expected = InvalidOptionConfigurationException.class)
    public void Given_OptionMapWithFileThatExistsButIsEmpty_When_ParsingPathWithGreaterThanZeroPredicate_Then_ShouldFailConstraintCheck()
        throws IOException {
        Path tempFile = Files.createTempFile("apiman-test", "bar");

        Map<String, String> map = new HashMap<String, String>() {{
            put("seychelles", tempFile.toAbsolutePath().toString());
        }};

        GenericOptionsParser opts = new GenericOptionsParser(map);

        // Will throw exception as file is empty
        opts.getPath(
            keys("seychelles"),
            null,
            Predicates.fileExists().and(Predicates.fileSizeGreaterThanZero()),
            "File must exist and be greater then zero in size"
        );
    }


    @Test
    public void Given_OptionMapWithNoMatchingKey_When_ParsingPath_Then_ShouldUseDefaultValue()
        throws IOException {
        Map<String, String> emptyOpts = new HashMap<>();

        Path tempFile = Files.createTempFile("apiman-test", "bar");
        Files.write(tempFile, "blah blah".getBytes(StandardCharsets.UTF_8));

        GenericOptionsParser opts = new GenericOptionsParser(emptyOpts);
        Path parsedPath = opts.getPath(
            keys("blahblahdoesnotexist"),
            tempFile,
            Predicates.fileExists().and(Predicates.fileSizeGreaterThanZero()),
            "File must exist and be greater then zero in size"
        );

        assertThat(parsedPath).exists();
        assertThat(parsedPath).hasContent("blah blah");
    }


}