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


package io.apiman.test.common.es;


import io.apiman.common.es.util.EsConstants;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

public final class EsTestUtil {

    private EsTestUtil() {}

    /**
     * Provide a test container for elasticsearch based on a system property or environment variable.
     * Use apiman.es.provider=opensearch or APIMAN_ES_PROVIDER=opensearch
     *
     * @return a testcontainer for elasticsearch tests
     */
    public static ElasticsearchContainer provideElasticsearchContainer() {
        String esImageType = "docker.elastic.co/elasticsearch/elasticsearch";
        String esProvider = Optional.ofNullable(System.getenv("APIMAN_ES_PROVIDER"))
            .orElseGet(() -> System.getProperty("apiman.es.provider"));

        if ("opensearch".equalsIgnoreCase(esProvider)) {
            return new ElasticsearchContainer(DockerImageName.parse("opensearchproject/opensearch")
                .withTag(EsConstants.getEsVersion().getProperty("apiman.opensearch-version"))
                .asCompatibleSubstituteFor(esImageType))
                .withEnv("discovery.type", "single-node")
                .withEnv("DISABLE_INSTALL_DEMO_CONFIG", "true")
                .withEnv("DISABLE_SECURITY_PLUGIN", "true");
        }
        return new ElasticsearchContainer(DockerImageName.parse(esImageType)
            .withTag(EsConstants.getEsVersion().getProperty("apiman.elasticsearch-version")))
            .withEnv("xpack.security.enabled", "false");
    }
}
