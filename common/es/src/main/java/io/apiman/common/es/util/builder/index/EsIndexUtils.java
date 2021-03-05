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

import static io.apiman.common.es.util.EsConstants.ES_MAPPING_TYPE_BINARY;
import static io.apiman.common.es.util.EsConstants.ES_MAPPING_TYPE_BOOLEAN;
import static io.apiman.common.es.util.EsConstants.ES_MAPPING_TYPE_DATE;
import static io.apiman.common.es.util.EsConstants.ES_MAPPING_TYPE_IP;
import static io.apiman.common.es.util.EsConstants.ES_MAPPING_TYPE_KEYWORD;
import static io.apiman.common.es.util.EsConstants.ES_MAPPING_TYPE_LONG;
import static io.apiman.common.es.util.EsConstants.ES_MAPPING_TYPE_OBJECT;
import static io.apiman.common.es.util.EsConstants.ES_MAPPING_TYPE_TEXT;

/**
 * Common ES index properties.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class EsIndexUtils {
    public static final EsIndexProperty LONG_PROP = EsIndexProperty.builder().setType(ES_MAPPING_TYPE_LONG).build();
    public static final EsIndexProperty DATE_PROP = EsIndexProperty.builder().setType(ES_MAPPING_TYPE_DATE).build();
    public static final EsIndexProperty IP_PROP = EsIndexProperty.builder().setType(ES_MAPPING_TYPE_IP).build();
    public static final EsIndexProperty BOOL_PROP = EsIndexProperty.builder().setType(ES_MAPPING_TYPE_BOOLEAN).build();
    public static final EsIndexProperty BIN_PROP = EsIndexProperty.builder().setType(ES_MAPPING_TYPE_BINARY).build();
    public static final EsIndexProperty OBJECT_PROP = EsIndexProperty.builder().setType(ES_MAPPING_TYPE_OBJECT).build();
    public static final EsField KEYWORD_PROP = KeywordEntryEs.builder().build();
    public static final EsIndexProperty TEXT_AND_KEYWORD_PROP_256 =
        EsIndexProperty.builder()
            .setType(ES_MAPPING_TYPE_TEXT)
            .addField(ES_MAPPING_TYPE_KEYWORD,
                KeywordEntryEs.builder().setIgnoreAbove(256).build())
            .build();
}
