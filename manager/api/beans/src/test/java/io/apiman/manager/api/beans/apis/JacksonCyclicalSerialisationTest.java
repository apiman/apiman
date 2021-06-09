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

package io.apiman.manager.api.beans.apis;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class JacksonCyclicalSerialisationTest {

    /**
     * We expect that serialization will succeed, but are not testing specific output.
     *
     * If this fails a stack overflow error or similar will occur due to the cyclical reference being chased
     * infinitely.
     */
    @Test(expected = Test.None.class)
    public void Given_CyclicalBeanReference_When_SerializingToJson_Then_ExpectSuccessfulSerializationWithIDReference()
        throws JsonProcessingException {
        ApiVersionBean apiVersion = new ApiVersionBean();
        apiVersion.setId(1234L);


        ApiDefinitionBean adb = new ApiDefinitionBean();
        adb.setId(1132L);
        adb.setData("wibbly, wobbly, woo".getBytes(StandardCharsets.UTF_8));
        adb.setApiVersion(apiVersion);

        apiVersion.apiDefinition = adb;

        // If we successfully serialize here, then the cycle detection worked.
        ObjectMapper om = new ObjectMapper();
        System.out.println(om.writeValueAsString(apiVersion));
        System.out.println(om.writeValueAsString(adb));









        //

    }
}