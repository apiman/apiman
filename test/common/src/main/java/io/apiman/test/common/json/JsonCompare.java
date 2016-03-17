/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.test.common.json;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Used to compare an expected JSON document to an actual one.
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("nls")
public class JsonCompare {

    private static final ObjectMapper jacksonParser = new ObjectMapper();

    private JsonMissingFieldType missingField = JsonMissingFieldType.ignore;
    private JsonArrayOrderingType arrayOrdering = JsonArrayOrderingType.strict;
    private boolean ignoreCase;
    private Stack<Object> currentPath = new Stack<Object>();
    
    /**
     * Constructor.
     */
    public JsonCompare() {
    }

    /**
     * Asserts that the JSON document matches what we expected.
     * 
     * Note: the input streams should be closed by the caller
     * @param expectedJson
     * @param actualJson
     */
    public void assertJson(InputStream expectedJson, InputStream actualJson) throws Exception {
        JsonNode expected = jacksonParser.readTree(expectedJson);
        JsonNode actual = jacksonParser.readTree(actualJson);
        assertJson(expected, actual);
    }

    /**
     * Asserts that the JSON document matches what we expected.
     * @param expectedJson
     * @param actualJson
     */
    public void assertJson(JsonNode expectedJson, JsonNode actualJson) {
        if (expectedJson instanceof ArrayNode) {
            JsonNode actualValue = actualJson;
            ArrayNode expectedArray = (ArrayNode) expectedJson;
            Assert.assertEquals(
                    message("Expected JSON array but found non-array [{0}] instead.",
                            actualValue.getClass().getSimpleName()),
                    expectedJson.getClass(), actualValue.getClass());
            ArrayNode actualArray = (ArrayNode) actualValue;
            Assert.assertEquals(message("Array size mismatch."), expectedArray.size(), actualArray.size());

            JsonNode [] expected = new JsonNode[expectedArray.size()];
            JsonNode [] actual = new JsonNode[actualArray.size()];
            for (int idx = 0; idx < expected.length; idx++) {
                expected[idx] = expectedArray.get(idx);
                actual[idx] = actualArray.get(idx);
            }
            // If strict ordering is disabled, then sort both arrays
            if (arrayOrdering == JsonArrayOrderingType.any) {
                Comparator<? super JsonNode> comparator = new Comparator<JsonNode>() {
                    @Override
                    public int compare(JsonNode o1, JsonNode o2) {
                        int cmp = o1.toString().compareTo(o2.toString());
                        if (cmp == 0)
                            cmp = 1;
                        return cmp;
                    }
                };
                Arrays.sort(expected, comparator);
                Arrays.sort(actual, comparator);
            }
            for (int idx = 0; idx < expected.length; idx++) {
                currentPath.push(idx);
                assertJson(expected[idx], actual[idx]);
                currentPath.pop();
            }
        } else {
            Iterator<Entry<String, JsonNode>> fields = expectedJson.fields();
            Set<String> expectedFieldNames = new HashSet<>();
            while (fields.hasNext()) {
                Entry<String, JsonNode> entry = fields.next();
                String expectedFieldName = entry.getKey();
                expectedFieldNames.add(expectedFieldName);
                JsonNode expectedValue = entry.getValue();
                currentPath.push(expectedFieldName);
                if (expectedValue instanceof TextNode) {
                    TextNode tn = (TextNode) expectedValue;
                    String expected = tn.textValue();
                    JsonNode actualValue = actualJson.get(expectedFieldName);

                    if (isIgnoreCase()) {
                        expected = expected.toLowerCase();
                        if (actualValue == null) {
                            actualValue = actualJson.get(expectedFieldName.toLowerCase());
                        }
                    }

                    Assert.assertNotNull(
                            message("Expected JSON text field \"{0}\" with value \"{1}\" but was not found.",
                                    expectedFieldName, expected),
                            actualValue);
                    Assert.assertEquals(
                            message("Expected JSON text field \"{0}\" with value \"{1}\" but found non-text [{2}] field with that name instead.",
                                    expectedFieldName, expected, actualValue.getClass().getSimpleName()),
                            TextNode.class, actualValue.getClass());
                    String actual = ((TextNode) actualValue).textValue();

                    if (isIgnoreCase()) {
                        if (actual != null) {
                            actual = actual.toLowerCase();
                        }
                    }

                    if (!expected.equals("*")) {
                        Assert.assertEquals(
                                message("Value mismatch for text field \"{0}\".", expectedFieldName),
                                expected, actual);
                    }
                } else if (expectedValue instanceof NumericNode) {
                    NumericNode numeric = (NumericNode) expectedValue;
                    Number expected = numeric.numberValue();
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull(
                            message("Expected JSON numeric field \"{0}\" with value \"{1}\" but was not found.",
                                    expectedFieldName, expected),
                            actualValue);
                    Assert.assertEquals(
                            message("Expected JSON numeric field \"{0}\" with value \"{1}\" but found non-numeric [{2}] field with that name instead.",
                                    expectedFieldName, expected, actualValue.getClass().getSimpleName()),
                            expectedValue.getClass(), actualValue.getClass());
                    Number actual = ((NumericNode) actualValue).numberValue();
                    Assert.assertEquals(message("Value mismatch for numeric field \"{0}\".", expectedFieldName), expected,
                            actual);
                } else if (expectedValue instanceof BooleanNode) {
                    BooleanNode bool = (BooleanNode) expectedValue;
                    Boolean expected = bool.booleanValue();
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull(
                            message("Expected JSON boolean field \"{0}\" with value \"{1}\" but was not found.",
                                    expectedFieldName, expected),
                            actualValue);
                    Assert.assertEquals(
                            message("Expected JSON boolean field \"{0}\" with value \"{1}\" but found non-boolean [{2}] field with that name instead.",
                                    expectedFieldName, expected, actualValue.getClass().getSimpleName()),
                            expectedValue.getClass(), actualValue.getClass());
                    Boolean actual = ((BooleanNode) actualValue).booleanValue();
                    Assert.assertEquals(
                            message("Value mismatch for boolean field \"{0}\".", expectedFieldName), expected,
                            actual);
                } else if (expectedValue instanceof ObjectNode) {
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull(message("Expected parent JSON field \"{0}\" but was not found.",
                            expectedFieldName), actualValue);
                    Assert.assertEquals(
                            message("Expected parent JSON field \"{0}\" but found field of type \"{1}\".",
                                    expectedFieldName, actualValue.getClass().getSimpleName()),
                            ObjectNode.class, actualValue.getClass());
                    assertJson(expectedValue, actualValue);
                } else if (expectedValue instanceof ArrayNode) {
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull(message("Expected JSON array field \"{0}\" but was not found.",
                            expectedFieldName), actualValue);
                    ArrayNode expectedArray = (ArrayNode) expectedValue;
                    Assert.assertEquals(
                            message("Expected JSON array field \"{0}\" but found non-array [{1}] field with that name instead.",
                                    expectedFieldName, actualValue.getClass().getSimpleName()),
                            expectedValue.getClass(), actualValue.getClass());
                    ArrayNode actualArray = (ArrayNode) actualValue;
                    Assert.assertEquals(message("Field \"{0}\" array size mismatch.", expectedFieldName),
                            expectedArray.size(), actualArray.size());
                    assertJson(expectedArray, actualArray);
                } else if (expectedValue instanceof NullNode) {
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull(
                            message("Expected Null JSON field \"{0}\" but was not found.", expectedFieldName),
                            actualValue);
                    Assert.assertEquals(
                            message("Expected Null JSON field \"{0}\" but found field of type \"{0}\".",
                                    expectedFieldName, actualValue.getClass().getSimpleName()),
                            NullNode.class, actualValue.getClass());
                } else {
                    Assert.fail(message("Unsupported field type: {0}", expectedValue.getClass().getSimpleName()));
                }
                currentPath.pop();
            }
            
            if (getMissingField() == JsonMissingFieldType.fail) {
                Set<String> actualFieldNames = new HashSet();
                Iterator<String> names = actualJson.fieldNames();
                while (names.hasNext()) {
                    actualFieldNames.add(names.next());
                }
                actualFieldNames.removeAll(expectedFieldNames);
                Assert.assertTrue(
                        message("Found unexpected fields: {0}", StringUtils.join(actualFieldNames, ", ")),
                        actualFieldNames.isEmpty());
            }
        }
    }
    
    /**
     * Format an assertion message, including the current path.
     * @param pattern
     * @param params
     */
    private String message(String pattern, Object ... params) {
        return path() + "::" + MessageFormat.format(pattern, params);
    }
    
    /**
     * Format the current path as a string.
     */
    private String path() {
        StringBuilder builder = new StringBuilder();
        builder.append("$ROOT");
        for (Object pathElement : currentPath) {
            if (pathElement instanceof Integer) {
                builder.append('[');
                builder.append(pathElement);
                builder.append(']');
            } else {
                builder.append('.');
                builder.append(pathElement);
            }
        }
        return builder.toString();
    }

    /**
     * @return the arrayOrdering
     */
    public JsonArrayOrderingType getArrayOrdering() {
        return arrayOrdering;
    }

    /**
     * @param arrayOrdering the arrayOrdering to set
     */
    public void setArrayOrdering(JsonArrayOrderingType arrayOrdering) {
        this.arrayOrdering = arrayOrdering;
    }

    /**
     * @return the ignoreCase
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**
     * @param ignoreCase the ignoreCase to set
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    /**
     * @return the missingField
     */
    public JsonMissingFieldType getMissingField() {
        return missingField;
    }

    /**
     * @param missingField the missingField to set
     */
    public void setMissingField(JsonMissingFieldType missingField) {
        this.missingField = missingField;
    }

}
