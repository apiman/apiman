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

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Used to compare an expected JSON document to an actual one.
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("nls")
public class JsonCompare {

    private static final ObjectMapper jacksonParser = new ObjectMapper();

    private JsonMissingFieldType missingField = JsonMissingFieldType.ignore;
    private JsonArrayOrderingType arrayOrdering = JsonArrayOrderingType.strict;
    private boolean compareNumericIds = true;
    private boolean ignoreCase = false;
    private Stack<Object> currentPath = new Stack<>();

    /**
     * Constructor.
     */
    public JsonCompare() {
    }

    public static class WildcardComparator extends DefaultComparator {

        private Map<String, String> idCorrelationMap = new LinkedHashMap<>();

        public WildcardComparator(JSONCompareMode mode) {
            super(mode);
        }

        @Override
        protected void compareJSONArrayOfJsonObjects(String key, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
            //super.compareJSONArrayOfJsonObjects(key, expected, actual, result);


            if (key.toLowerCase().endsWith("id")) {
                System.out.println("Ends with? 12312321 " + key);
                return;
            }

            super.recursivelyCompareJSONArray(key, expected, actual, result);
        }

        // AUDITS SECTION?!
//        @Override
//        public void compareJSONArray(String prefix, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
//            super.compareJSONArray(prefix, expected, actual, result);
//        }

        @Override
        public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result)
                throws JSONException {

                System.out.println("prefix = " + prefix);

            if (prefix.toLowerCase().equals("id")) {
                System.out.println("Ignoring/autoaccepting " + prefix);
                return;
            }


//            if (expectedValue instanceof String && ((String)expectedValue).startsWith("CORRELATE_VALUE_")) {
//                String correlationIndex = ((String)expectedValue).substring(16);
//
//                // If an entry has already been set, we'll expect all subsequent values for the same key to
//                // be the same. Effectively, the first value seen for that key sets the expectation.
//                if (idCorrelationMap.containsKey(correlationIndex)) {
//                    String correlationValue = idCorrelationMap.get(correlationIndex);
//                    String actualCorrelationValue = String.valueOf(actualValue);
//
//                    // If they match, continue as this indicates success.
//                    if (correlationValue.equalsIgnoreCase(actualCorrelationValue)) {
//                        System.out.println("Correlated ..." + correlationValue + " on " + correlationIndex);
//                        return;
//                    } else {
//                        // If not match, throw exception. TODO better message
//                        result.fail("A field was expected to have a correlated value, but it did not match", correlationValue, actualCorrelationValue);
//                        throw new RuntimeException("Correlation failure");
//                    }
//                } else {
//                    System.out.println("Set correlationIndex:value " + correlationIndex + ":" + String.valueOf(actualValue));
//                    // Set expectation as the first value seen. E.g. 1 = foo-bar-key
//                    idCorrelationMap.put(correlationIndex, String.valueOf(actualValue));
//                    return;
//                }
//            }

            if (expectedValue instanceof String && ((String)expectedValue).equals("*") && actualValue != null) {
                // Wildcard: We want the key to exist and have a value; we don't want to assert a specific value.
                return;
            }
            super.compareValues(prefix, expectedValue, actualValue, result);
        }
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
        //String expectedS = IOUtils.toString(expectedJson);
        //String actualS = IOUtils.toString(actualJson);

        assertJson(expected, actual);
        //assertJson(expected, actual);

//        Configuration config = Configuration.empty().when(Option.IGNORING_ARRAY_ORDER,
//                Option.IGNORING_EXTRA_ARRAY_ITEMS,
//                Option.IGNORING_EXTRA_FIELDS);
//
//        JsonAssert.assertJsonEquals(expected, actual, config);
    }

    public static void main(String[] args) throws Exception {
        //JsonCompare jc = new JsonCompare();
        //jc.assertJson(expectedJson, actualJson);

        //new JsonCompare().assertJson();


//
//        new JsonCompare().assertJson(
//                "{\n" +
//                "    \"Orgs\": [{\n" +
//                "        \"a\": \"b\",\n" +
//                "        \"thingId\": \"CORRELATE_VALUE_1\",\n" +
//                "        \"thing2Id\": \"CORRELATE_VALUE_1\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "        \"a\": \"c\",\n" +
//                "        \"thingId\": \"CORRELATE_VALUE_2\",\n" +
//                "        \"thing2Id\": \"CORRELATE_VALUE_1\"\n" +
//                "    }]\n" +
//                "}\n",
//
//                "{\n" +
//                "    \"Orgs\": [{\n" +
//                "        \"a\": \"c\",\n" +
//                "        \"thingId\": 123,\n" +
//                "        \"thing2Id\": 123\n" +
//                "    },\n" +
//                "    {\n" +
//                "        \"a\": \"b\",\n" +
//                "        \"thingId\": 999,\n" +
//                "        \"thing2Id\": 123\n" +
//                "    }]\n" +
//                "}");

//        new JsonCompare().assertJson(
//                "{\"organization\":{\"id\":\"Organization1\"},\"id\":\"API1\",\"name\":\"API 1\",\"description\":\"CORRELATE_VALUE_1\",\"createdBy\":\"CORRELATE_VALUE_1\"}\n"
//               ,
//                "{\"organization\":{\"id\":\"Organization1\",\"name\":\"Organization 1\",\"description\":\"admin\",\"createdBy\":\"admin\",\"createdOn\":1527084576734,\"modifiedBy\":\"admin\","
//                + "\"modifiedOn\":1527084576734},\"id\":\"API1\",\"name\":\"API 1\",\"description\":\"adminx\","
//                + "\"createdBy\":\"admin\",\"createdOn\":1527084580868,\"numPublished\":null}"
//                );

      InputStream ise = new FileInputStream("/tmp/expectedOriginal.json");
      String ises = IOUtils.toString(ise, Charset.defaultCharset());
      InputStream isa = new FileInputStream("/tmp/actualOrginal.json"); // nice typo
      String isas = IOUtils.toString(isa, Charset.defaultCharset());

      //new JsonCompare().assertJson(ise, isa);

      JSONAssert.assertEquals(ises, isas, false);


//        String expected = "";
//
//        String actual = "";
//
//        Configuration config = Configuration.empty().when(Option.IGNORING_ARRAY_ORDER,
//                Option.IGNORING_EXTRA_ARRAY_ITEMS,
//                Option.IGNORING_EXTRA_FIELDS,
//                Option.IGNORING_VALUES);
//
//        JsonAssert.assertJsonEquals(expected, actual, config);

        System.out.println("seems it worked?");

    }

    public void assertJson(String expectedJson, String actualJson) {
        try {
            //throw new JSONException("finish me");
            JSONAssert.assertEquals(expectedJson, actualJson, new WildcardComparator(JSONCompareMode.LENIENT));
        } catch (JSONException e1) {
            throw new RuntimeException(e1);
        }

    }

    /**
     * Asserts that the JSON document matches what we expected.
     * @param expectedJson
     * @param actualJson
     */
    public void assertJson(JsonNode expectedJson, JsonNode actualJson) {

        try {
            String expectedS = jacksonParser.writeValueAsString(expectedJson);
            String actualS = jacksonParser.writeValueAsString(actualJson);

            JSONAssert.assertEquals(expectedS, actualS, new WildcardComparator(JSONCompareMode.LENIENT));
//            Configuration config = Configuration.empty().when(Option.IGNORING_ARRAY_ORDER,
//                    Option.IGNORING_EXTRA_ARRAY_ITEMS,
//                    Option.IGNORING_EXTRA_FIELDS);
//
//            JsonAssert.assertJsonEquals(expectedS, actualS, config);
        } catch (JsonProcessingException e1) {
            throw new RuntimeException(e1);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


//
//        if (expectedJson instanceof ArrayNode) {
//            JsonNode actualValue = actualJson;
//            ArrayNode expectedArray = (ArrayNode) expectedJson;
//            Assert.assertEquals(
//                    message("Expected JSON array but found non-array [{0}] instead.",
//                            actualValue.getClass().getSimpleName()),
//                    expectedJson.getClass(), actualValue.getClass());
//            ArrayNode actualArray = (ArrayNode) actualValue;
//            Assert.assertEquals(message("Array size mismatch."), expectedArray.size(), actualArray.size());
//
//            JsonNode [] expected = new JsonNode[expectedArray.size()];
//            JsonNode [] actual = new JsonNode[actualArray.size()];
//            for (int idx = 0; idx < expected.length; idx++) {
//                expected[idx] = expectedArray.get(idx);
//                actual[idx] = actualArray.get(idx);
//            }
//            // If strict ordering is disabled, then sort both arrays
//            if (getArrayOrdering() == JsonArrayOrderingType.any) {
//                Comparator<? super JsonNode> comparator = new Comparator<JsonNode>() {
//                    @Override
//                    public int compare(JsonNode o1, JsonNode o2) {
//                        String str1 = o1.asText();
//                        String str2 = o2.asText();
//                        if (o1.isObject() && o2.isObject()) {
//                            // Try name (PermissionBean only)
//                            JsonNode o1NameNode = o1.get("name");
//                            JsonNode o2NameNode = o2.get("name");
//                            if (o1NameNode != null && o2NameNode != null) {
//                                str1 = o1NameNode.asText();
//                                str2 = o2NameNode.asText();
//                            }
//
//                            // Try username (UserBean only)
//                            JsonNode o1UsernameNode = o1.get("username");
//                            JsonNode o2UsernameNode = o2.get("username");
//                            if (o1UsernameNode != null && o2UsernameNode != null) {
//                                str1 = o1UsernameNode.asText();
//                                str2 = o2UsernameNode.asText();
//                            }
//
//                            // Try version (*VersionBeans)
//                            JsonNode o1VersionNode = o1.get("version");
//                            JsonNode o2VersionNode = o2.get("version");
//                            if (o1VersionNode != null && o2VersionNode != null) {
//                                str1 = o1VersionNode.asText();
//                                str2 = o2VersionNode.asText();
//                            }
//
//                            // Try OrganizationBean.id (Orgs)
//                            JsonNode o1OrgNode = o1.get("OrganizationBean");
//                            JsonNode o2OrgNode = o2.get("OrganizationBean");
//                            if (o1OrgNode != null && o2OrgNode != null) {
//                                str1 = o1OrgNode.get("id").asText();
//                                str2 = o2OrgNode.get("id").asText();
//                            }
//
//                            // Try ClientBean.id (Orgs)
//                            JsonNode o1ClientNode = o1.get("ClientBean");
//                            JsonNode o2ClientNode = o2.get("ClientBean");
//                            if (o1ClientNode != null && o2ClientNode != null) {
//                                str1 = o1ClientNode.get("id").asText();
//                                str2 = o2ClientNode.get("id").asText();
//                            }
//
//                            // Try PlanBean.id (Orgs)
//                            JsonNode o1PlanNode = o1.get("PlanBean");
//                            JsonNode o2PlanNode = o2.get("PlanBean");
//                            if (o1PlanNode != null && o2PlanNode != null) {
//                                str1 = o1PlanNode.get("id").asText();
//                                str2 = o2PlanNode.get("id").asText();
//                            }
//
//                            // Try ApiBean.id (Orgs)
//                            JsonNode o1ApiNode = o1.get("ApiBean");
//                            JsonNode o2ApiNode = o2.get("ApiBean");
//                            if (o1ApiNode != null && o2ApiNode != null) {
//                                str1 = o1ApiNode.get("id").asText();
//                                str2 = o2ApiNode.get("id").asText();
//                            }
//
//                            // Try Id (all other beans)
//                            JsonNode o1IdNode = o1.get("id");
//                            JsonNode o2IdNode = o2.get("id");
//                            if (o1IdNode != null && o2IdNode != null) {
//                                if (o1IdNode.isNumber()) {
//                                    return new Long(o1IdNode.asLong()).compareTo(o2IdNode.asLong());
//                                }
//                                str1 = o1IdNode.asText();
//                                str2 = o2IdNode.asText();
//                            }
//                        }
//                        int cmp = str1.compareTo(str2);
//                        if (cmp == 0)
//                            cmp = 1;
//                        return cmp;
//                    }
//                };
//                Arrays.sort(expected, comparator);
//                Arrays.sort(actual, comparator);
//            }
//            for (int idx = 0; idx < expected.length; idx++) {
//                currentPath.push(idx);
//                assertJson(expected[idx], actual[idx]);
//                currentPath.pop();
//            }
//        } else if (expectedJson instanceof ValueNode) {
//            // If we have a value by itself (e.g. from an array that contained leaf values)
//            Assert.assertEquals(message("Expected leaf JSON value did not match."), expectedJson, actualJson);
//        } else {
//            Iterator<Entry<String, JsonNode>> fields = expectedJson.fields();
//            Set<String> expectedFieldNames = new HashSet<>();
//            while (fields.hasNext()) {
//                Entry<String, JsonNode> entry = fields.next();
//                String expectedFieldName = entry.getKey();
//                expectedFieldNames.add(expectedFieldName);
//                JsonNode expectedValue = entry.getValue();
//                currentPath.push(expectedFieldName);
//                if (expectedValue instanceof TextNode) {
//                    TextNode tn = (TextNode) expectedValue;
//                    String expected = tn.textValue();
//                    JsonNode actualValue = actualJson.get(expectedFieldName);
//
//                    if (isIgnoreCase()) {
//                        expected = expected.toLowerCase();
//                        if (actualValue == null) {
//                            actualValue = actualJson.get(expectedFieldName.toLowerCase());
//                        }
//                    }
//
//                    Assert.assertNotNull(
//                            message("Expected JSON text field \"{0}\" with value \"{1}\" but was not found.",
//                                    expectedFieldName, expected),
//                            actualValue);
//                    Assert.assertEquals(
//                            message("Expected JSON text field \"{0}\" with value \"{1}\" but found non-text [{2}] field with that name instead.",
//                                    expectedFieldName, expected, actualValue.getClass().getSimpleName()),
//                            TextNode.class, actualValue.getClass());
//                    String actual = ((TextNode) actualValue).textValue();
//
//                    if (isIgnoreCase()) {
//                        if (actual != null) {
//                            actual = actual.toLowerCase();
//                        }
//                    }
//
//                    if (!expected.equals("*")) {
//                        Assert.assertEquals(
//                                message("Value mismatch for text field \"{0}\".", expectedFieldName),
//                                expected, actual);
//                    }
//                } else if (expectedValue.isNumber()) {
//                    NumericNode numeric = (NumericNode) expectedValue;
//                    Number expected = numeric.numberValue();
//                    JsonNode actualValue = actualJson.get(expectedFieldName);
//                    try {
//                        Assert.assertNotNull(
//                                message("Expected JSON numeric field \"{0}\" with value \"{1}\" but was not found.",
//                                        expectedFieldName, expected),
//                                actualValue);
//                    } catch (Error e) {
//                        throw e;
//                    }
//                    Assert.assertTrue(
//                            message("Expected JSON numeric field \"{0}\" with value \"{1}\" but found non-numeric [{2}] field with that name instead.",
//                                    expectedFieldName, expected, actualValue.getClass().getSimpleName()),
//                            actualValue.isNumber());
//                    Number actual = ((NumericNode) actualValue).numberValue();
//                    if (!"id".equals(expectedFieldName) || isCompareNumericIds()) {
//                        Assert.assertEquals(message("Value mismatch for numeric field \"{0}\".", expectedFieldName), expected,
//                                actual);
//                    }
//                } else if (expectedValue instanceof BooleanNode) {
//                    BooleanNode bool = (BooleanNode) expectedValue;
//                    Boolean expected = bool.booleanValue();
//                    JsonNode actualValue = actualJson.get(expectedFieldName);
//                    Assert.assertNotNull(
//                            message("Expected JSON boolean field \"{0}\" with value \"{1}\" but was not found.",
//                                    expectedFieldName, expected),
//                            actualValue);
//                    Assert.assertEquals(
//                            message("Expected JSON boolean field \"{0}\" with value \"{1}\" but found non-boolean [{2}] field with that name instead.",
//                                    expectedFieldName, expected, actualValue.getClass().getSimpleName()),
//                            expectedValue.getClass(), actualValue.getClass());
//                    Boolean actual = ((BooleanNode) actualValue).booleanValue();
//                    Assert.assertEquals(
//                            message("Value mismatch for boolean field \"{0}\".", expectedFieldName), expected,
//                            actual);
//                } else if (expectedValue instanceof ObjectNode) {
//                    JsonNode actualValue = actualJson.get(expectedFieldName);
//                    Assert.assertNotNull(message("Expected parent JSON field \"{0}\" but was not found.",
//                            expectedFieldName), actualValue);
//                    Assert.assertEquals(
//                            message("Expected parent JSON field \"{0}\" but found field of type \"{1}\".",
//                                    expectedFieldName, actualValue.getClass().getSimpleName()),
//                            ObjectNode.class, actualValue.getClass());
//                    assertJson(expectedValue, actualValue);
//                } else if (expectedValue instanceof ArrayNode) {
//                    JsonNode actualValue = actualJson.get(expectedFieldName);
//                    Assert.assertNotNull(message("Expected JSON array field \"{0}\" but was not found.",
//                            expectedFieldName), actualValue);
//                    ArrayNode expectedArray = (ArrayNode) expectedValue;
//                    Assert.assertEquals(
//                            message("Expected JSON array field \"{0}\" but found non-array [{1}] field with that name instead.",
//                                    expectedFieldName, actualValue.getClass().getSimpleName()),
//                            expectedValue.getClass(), actualValue.getClass());
//                    ArrayNode actualArray = (ArrayNode) actualValue;
//                    Assert.assertEquals(message("Field \"{0}\" array size mismatch.", expectedFieldName),
//                            expectedArray.size(), actualArray.size());
//                    assertJson(expectedArray, actualArray);
//                } else if (expectedValue instanceof NullNode) {
//                    JsonNode actualValue = actualJson.get(expectedFieldName);
//                    Assert.assertNotNull(
//                            message("Expected Null JSON field \"{0}\" but was not found.", expectedFieldName),
//                            actualValue);
//                    Assert.assertEquals(
//                            message("Expected Null JSON field \"{0}\" but found field of type \"{0}\".",
//                                    expectedFieldName, actualValue.getClass().getSimpleName()),
//                            NullNode.class, actualValue.getClass());
//                } else {
//                    Assert.fail(message("Unsupported field type: {0}", expectedValue.getClass().getSimpleName()));
//                }
//                currentPath.pop();
//            }
//
//            if (getMissingField() == JsonMissingFieldType.fail) {
//                Set<String> actualFieldNames = new HashSet<>();
//                Iterator<String> names = actualJson.fieldNames();
//                while (names.hasNext()) {
//                    actualFieldNames.add(names.next());
//                }
//                actualFieldNames.removeAll(expectedFieldNames);
//                Assert.assertTrue(
//                        message("Found unexpected fields: {0}", StringUtils.join(actualFieldNames, ", ")),
//                        actualFieldNames.isEmpty());
//            }
//        }
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

    /**
     * @return the compareNumericIds
     */
    public boolean isCompareNumericIds() {
        return compareNumericIds;
    }

    /**
     * @param compareNumericIds the compareNumericIds to set
     */
    public void setCompareNumericIds(boolean compareNumericIds) {
        this.compareNumericIds = compareNumericIds;
    }

}
