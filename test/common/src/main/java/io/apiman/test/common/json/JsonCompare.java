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
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    /**
     * Constructor.
     */
    public JsonCompare() {
    }

    public class WildcardComparator extends DefaultComparator {

        private Map<String, String> idCorrelationMap = new LinkedHashMap<>();

        public WildcardComparator(JSONCompareMode mode) {
            super(mode);
        }

        @Override
        protected void compareJSONArrayOfJsonObjects(String key, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
            if (key.toLowerCase().endsWith("id")) {
                return;
            }

            if (key.equals("PolicyDefinitions")){
                // With this we will achieve that the order of the actual an expected json is always the same
                expected = sortJsonArrayAlphabetically(expected, "policyImpl");
                actual = sortJsonArrayAlphabetically(actual,"policyImpl");
            }

            super.recursivelyCompareJSONArray(key, expected, actual, result);
        }

        @Override
        public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result)
                throws JSONException {

            if (!compareNumericIds &&
                    (prefix.toLowerCase().equals("id") || prefix.toLowerCase().endsWith(".id"))) {
                return;

            }

            // NB: certain out-of-orderings could cause us to incorrectly set the wrong resolved value (by mistaking very similar
            // objects or resolving the variable too early before looking at other distinguishing fields).
            // This is difficult to fix as there is no way of explicitly delaying the value substitution until last, and is impossible to
            // disambiguate in certain situations.
            if (expectedValue instanceof String && ((String)expectedValue).startsWith("CORRELATE_VALUE_")) {
                String correlationIndex = ((String)expectedValue).substring(16);

                // If an entry has already been set, we'll expect all subsequent values for the same key to
                // be the same. Effectively, the first value seen for that key sets the expectation.
                if (idCorrelationMap.containsKey(correlationIndex)) {
                    String correlationValue = idCorrelationMap.get(correlationIndex);
                    String actualCorrelationValue = String.valueOf(actualValue);

                    // If they match, continue as this indicates success.
                    if (correlationValue.equalsIgnoreCase(actualCorrelationValue)) {
                        System.out.println("Correlated ..." + correlationValue + " on " + correlationIndex);
                        return;
                    } else {
                        result.fail("A field's resolved runtime value, but it did not correlate as expected", correlationValue, actualCorrelationValue);
                        throw new RuntimeException("Correlation failure");
                    }
                } else {
                    System.out.println("Set correlationIndex:value " + correlationIndex + ":" + String.valueOf(actualValue));
                    // Set expectation as the first value seen. E.g. 1 = foo-bar-key
                    idCorrelationMap.put(correlationIndex, String.valueOf(actualValue));
                    return;
                }
            }

            if (expectedValue instanceof String && ((String)expectedValue).equals("*") && actualValue != null) {
                // Wildcard: We want the key to exist and have a value; we don't want to assert a specific value.
                return;
            }
            super.compareValues(prefix, expectedValue, actualValue, result);
        }
    }

    /**
     * This will sort a json array in alphabetical order.
     * @param jsonArray
     * @return sorted jsonArray
     * @throws JSONException
     */
    private JSONArray sortJsonArrayAlphabetically(JSONArray jsonArray, String key) throws JSONException {

        List<JSONObject> jsonValues = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            jsonValues.add(jsonArray.getJSONObject(i));
        }

        Collections.sort(jsonValues, (o1, o2) -> {
            String valA;
            String valB;
            try {
                valA = (String) o1.get(key);
                valB = (String) o2.get(key);
            }
            catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return valA.compareTo(valB);
        });

        return new JSONArray(jsonValues);
    }


    /**
     * Asserts that the JSON document matches what we expected.
     *
     * Note: the input streams should be closed by the caller
     * @param expectedJson expected
     * @param actualJson actual
     * @throws Exception error when parsing json or validating structure
     */
    public void assertJson(InputStream expectedJson, InputStream actualJson) throws Exception  {
        JsonNode expected = jacksonParser.readTree(expectedJson);
        JsonNode actual = jacksonParser.readTree(actualJson);
        assertJson(expected, actual);
    }

    public void assertJson(String expectedJson, String actualJson) {
        try {
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
        } catch (JsonProcessingException | JSONException e1) {
            throw new RuntimeException(e1);
        }
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
