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

package io.apiman.common.util;

import org.junit.Assert;
import org.junit.Test;

import io.apiman.common.util.ApimanPathUtils.ApiRequestPathInfo;

public class ApimanPathUtilsTest {

	@Test
	public void testParseApiRequestPath() {
		ApiRequestPathInfo path = ApimanPathUtils.parseApiRequestPath(null, "application/json", "/OrgId/ApiId/VER/path/to/thing");
		Assert.assertEquals("OrgId", path.orgId);
		Assert.assertEquals("ApiId", path.apiId);
		Assert.assertEquals("VER", path.apiVersion);
		Assert.assertEquals("/path/to/thing", path.resource);

		path = ApimanPathUtils.parseApiRequestPath(null, "application/json", "/OrgId/ApiId/VER");
		Assert.assertEquals("OrgId", path.orgId);
		Assert.assertEquals("ApiId", path.apiId);
		Assert.assertEquals("VER", path.apiVersion);
		Assert.assertEquals(null, path.resource);

		path = ApimanPathUtils.parseApiRequestPath(null, "application/json", "/OrgId/ApiId/VER/");
		Assert.assertEquals("OrgId", path.orgId);
		Assert.assertEquals("ApiId", path.apiId);
		Assert.assertEquals("VER", path.apiVersion);
		Assert.assertEquals("/", path.resource);

		path = ApimanPathUtils.parseApiRequestPath("VER", "application/json", "/OrgId/ApiId/path/to/thing");
		Assert.assertEquals("OrgId", path.orgId);
		Assert.assertEquals("ApiId", path.apiId);
		Assert.assertEquals("VER", path.apiVersion);
		Assert.assertEquals("/path/to/thing", path.resource);

		path = ApimanPathUtils.parseApiRequestPath("VER", "application/json", "/OrgId/ApiId");
		Assert.assertEquals("OrgId", path.orgId);
		Assert.assertEquals("ApiId", path.apiId);
		Assert.assertEquals("VER", path.apiVersion);
		Assert.assertEquals(null, path.resource);

	}

}
