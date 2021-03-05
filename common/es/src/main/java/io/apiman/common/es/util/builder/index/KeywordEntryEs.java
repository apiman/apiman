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

/**
 * An ES keyword entry
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class KeywordEntryEs extends EsField {
    KeywordEntryEs(EsFieldBuilder builder) {
        super(builder);
    }

    /**
     * Build an instance of {@link KeywordEntryEs}
     */
    public static EsFieldBuilder builder() {
        return EsField.builder().setType("keyword");
    }

    @Override
    public boolean isKeyword() {
        return true;
    }
}
