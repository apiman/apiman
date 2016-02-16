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

package io.apiman.gateway.engine.es.util;

/**
 * Some util methods for doing elastic queries.
 * @author ewittman
 */
public class ElasticQueryUtil {
    
    /**
     * Constructor.
     */
    private ElasticQueryUtil() {
    }
    
    @SuppressWarnings("nls")
    public static String queryContractsByClient(String orgId, String clientId, String version) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("  \"query\": {\n");
        builder.append("    \"filtered\": { \n");
        builder.append("      \"query\" : {\n");
        builder.append("        \"match_all\" : { }\n");
        builder.append("      },\n");
        builder.append("      \"filter\": {\n");
        builder.append("        \"and\" : {\n");
        builder.append("          \"filters\" : [ {\n");
        builder.append("            \"term\" : {\n");
        builder.append("              \"client.organizationId\" : \"" + orgId + "\"\n");
        builder.append("            }\n");
        builder.append("          }, {\n");
        builder.append("            \"term\" : {\n");
        builder.append("              \"client.clientId\" : \"" + clientId + "\"\n");
        builder.append("            }\n");
        builder.append("          }, {\n");
        builder.append("            \"term\" : {\n");
        builder.append("              \"client.version\" : \"" + version + "\"\n");
        builder.append("            }\n");
        builder.append("          } ]\n");
        builder.append("        }\n");
        builder.append("      }\n");
        builder.append("    }\n");
        builder.append("  }\n");
        builder.append("}\n");
        return builder.toString();
    }

}
