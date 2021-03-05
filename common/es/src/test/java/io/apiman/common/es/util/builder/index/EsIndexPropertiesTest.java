/*
 * Copyright 2021 Scheer PAS Schweiz AG
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
package io.apiman.common.es.util.builder.index;

import static io.apiman.common.es.util.builder.index.EsIndexUtils.BOOL_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.DATE_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.IP_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.KEYWORD_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.LONG_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.TEXT_AND_KEYWORD_PROP_256;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.common.es.util.EsConstants;
import org.junit.Test;

/**
 * Test that index mappings produce ES JSON definition that we expect
 */
public class EsIndexPropertiesTest {

    @Test
    public void basicPropertiesMap() throws JsonProcessingException {
        EsIndexProperties propertiesMap = EsIndexProperties.builder()
            .addProperty(EsConstants.ES_FIELD_API_DURATION, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_API_END, DATE_PROP)
            .addProperty(EsConstants.ES_FIELD_API_ID,  KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_API_ORG_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_API_START, DATE_PROP)
            .addProperty(EsConstants.ES_FIELD_API_VERSION, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_BYTES_DOWNLOADED, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_BYTES_UPLOADED, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_CLIENT_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_CLIENT_ORG_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_CLIENT_VERSION, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_CONTRACT_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_ERROR, BOOL_PROP)
            .addProperty(EsConstants.ES_FIELD_ERROR_MESSAGE, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_FAILURE, BOOL_PROP)
            .addProperty(EsConstants.ES_FIELD_FAILURE_CODE, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_FAILURE_REASON, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_METHOD, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_PLAN_ID, KEYWORD_PROP)
            .addProperty(EsConstants.ES_FIELD_REMOTE_ADDR, IP_PROP)
            .addProperty(EsConstants.ES_FIELD_REQUEST_DURATION, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_REQUEST_END, DATE_PROP)
            .addProperty(EsConstants.ES_FIELD_REQUEST_START, DATE_PROP)
            .addProperty(EsConstants.ES_FIELD_RESOURCE, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_RESPONSE_CODE, LONG_PROP)
            .addProperty(EsConstants.ES_FIELD_RESPONSE_MESSAGE, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_URL, TEXT_AND_KEYWORD_PROP_256)
            .addProperty(EsConstants.ES_FIELD_USER, TEXT_AND_KEYWORD_PROP_256)
        .build();

        ObjectMapper om = new ObjectMapper();
        String result = om.writerWithDefaultPrettyPrinter().writeValueAsString(propertiesMap);

        // See resources/index-samples/OutputIndex.json for expectation JSON
        assertThatJson(result).isEqualTo(resource("index-samples/OutputIndex.json"));
    }

}