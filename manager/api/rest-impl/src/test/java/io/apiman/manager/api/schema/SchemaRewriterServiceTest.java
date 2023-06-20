/*
 * Copyright 2023 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

package io.apiman.manager.api.schema;

import com.google.common.io.FileBackedOutputStream;
import io.apiman.manager.api.beans.apis.ApiDefinitionType;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class SchemaRewriterServiceTest {

    private final SchemaRewriterService service = new SchemaRewriterService();

    @Test
    public void testRewriteCache() throws Exception {
        ApiVersionBean aV = new ApiVersionBean();
        aV.setId(1L);
        aV.setDefinitionType(ApiDefinitionType.External); // use External for noop rewriter
        aV.setModifiedOn(new Date());

        int fileSize = getClass().getClassLoader().getResourceAsStream("schema/swagger-petstore.json").readAllBytes().length;

        // cache with noop rewriter
        FileBackedOutputStream stream = service.rewrite(aV, getClass().getClassLoader().getResourceAsStream("schema/swagger-petstore.json"));
        Assert.assertEquals(fileSize, stream.asByteSource().size());

        // should return cached value even with different input as the modified timestamp (cacheKey) is the same
        stream = service.rewrite(aV, getClass().getClassLoader().getResourceAsStream("schema/swagger-petstore-modified.json"));
        Assert.assertEquals(fileSize, stream.asByteSource().size());

        // change modifiedOn - should return new file with bigger size
        aV.setModifiedOn(new Date());
        stream = service.rewrite(aV, getClass().getClassLoader().getResourceAsStream("schema/swagger-petstore-modified.json"));
        Assert.assertTrue(fileSize < stream.asByteSource().size());
    }
}
