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

package io.apiman.migration.fix_pre_21;

import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.download.DownloadBean;
import io.apiman.manager.api.beans.system.SystemStatusBean;
import io.apiman.migration.util.LoggingMixin;
import io.apiman.migration.util.OkHttpUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ComparableVersion;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

/**
 * Enrich pre-2.1.0.Final release.
 *
 * Adds the missing ApiDefinition field to older Apiman export files.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Command(name = "upgrade",
    description = "Enrich a pre-2.1.0 Apiman export file with missing information queried from the older version "
        + "of Apiman. You can then import this enriched export into the newer version of Apiman and the "
        + "missing fields will appear. ")
public class EnrichPre21ExportCommand implements Callable<Integer> {
    private static final Logger LOGGER = LogManager.getLogger(EnrichPre21ExportCommand.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Mixin
    LoggingMixin loggingMixin;

    @Option(names = { "--username" }, required = true, description = "Apiman Manager admin username")
    String username;

    @Option(names = { "--password" }, required = true, description = "Apiman Manager admin password")
    String password;

    @Option(names = { "--endpoint" }, required = true, description = "Apiman Manager API HTTP endpoint. Example: http://localhost:8080/apiman")
    String endpoint;

    @Option(names = { "--output" }, required = true, description = "Where to write the enriched JSON file")
    File output;

    @Option(names = { "--overwrite" }, description = "Overwrite the output file if it already exists")
    boolean overwriteIfExists = false;

    @Option(names = { "-k", "--trust-all" }, description = "Trust all certificates when connecting to endpoint")
    boolean trustAll = false;

    private OkHttpClient client;

    @Override
    public Integer call() throws Exception {
        client = OkHttpUtils.createClient(trustAll);
        checkVersion();
        DownloadBean downloadBean = triggerExport();
        File exportJson = downloadExportJson(downloadBean);
        JsonNode rootNode = enrichWithSchema(exportJson);

        if (Files.exists(output.toPath())) {
            if (overwriteIfExists) {
                LOGGER.debug("Deleting existing file at {}", output.getAbsolutePath());
                Files.delete(output.toPath());
            } else {
                throw new FileAlreadyExistsException("File " + output.getAbsolutePath() + " already exists. "
                    + "Hint: you can use --overwrite");
            }
        }

        LOGGER.info("Writing result of enrichment to {}", output.getAbsolutePath());
        OBJECT_MAPPER.writeTree(OBJECT_MAPPER.createGenerator(output, JsonEncoding.UTF8), rootNode);
        return 0;
    }

    private void checkVersion() throws Exception {
        HttpUrl statusUrl = HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("system")
            .addPathSegment("status")
            .build();

        Request request = buildRequest(statusUrl);
        Call call = client.newCall(request);
        Response response = call.execute();

        checkStatusOk(response);

        SystemStatusBean systemStatus = OBJECT_MAPPER.readValue(response.body().bytes(), SystemStatusBean.class);

        if (!systemStatus.isUp()) {
            throw new IllegalStateException("Server down " + systemStatus);
        }

        ComparableVersion systemVersion = new ComparableVersion(systemStatus.getVersion());
        ComparableVersion maxVersion = new ComparableVersion("2.0.0.Final");

        if (systemVersion.compareTo(maxVersion) > 0) {
            throw new IllegalArgumentException("Server version is " + systemVersion + ". Any version after "
                + maxVersion + " does not need to apply this specific migration.");
        }
    }

    private JsonNode enrichWithSchema(File jsonFile) throws Exception {
        JsonNode root = OBJECT_MAPPER.readTree(jsonFile);
        new ApimanExportVisitor(root) {

            @Override
            public void visitApiVersion(String orgId, String apiId, JsonNode apiVersionNode)
                throws Exception {

                if (apiVersionNode.hasNonNull("ApiDefinition")) {
                    throw new IllegalArgumentException("The file provided already seems to have "
                        + "'ApiDefinition' entries. This migration helper will likely not be useful");
                }

                ApiVersionBean avb = OBJECT_MAPPER.treeToValue(apiVersionNode.get("ApiVersionBean"), ApiVersionBean.class);
                LOGGER.debug("Will look up schema for Api Version with ID {}", avb.getId());
                getSchema(orgId, apiId, avb).ifPresent(schema -> {
                    LOGGER.debug("Found schema of size: " + schema.capacity());
                    LOGGER.info("Adding an ApiDefinition to API Version: {}", avb.getId());
                    ((ObjectNode) apiVersionNode).put("ApiDefinition", schema.array());
                });
            }
        };
        return root;
    }

    private Optional<ByteBuffer> getSchema(String orgId, String apiId, ApiVersionBean avb) throws IOException {
        // {organizationId}/apis/{apiId}/versions/{version}/definition
        HttpUrl getSchemaUrl = HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("organizations")
            .addPathSegment(orgId)
            .addPathSegment("apis")
            .addPathSegment(apiId)
            .addPathSegment("versions")
            .addPathSegment(avb.getVersion())
            .addPathSegment("definition")
            .build();

        Request request = buildRequest(getSchemaUrl);
        Call exportCall = client.newCall(request);
        Response getSchemaResponse = exportCall.execute();

        if (getSchemaResponse.code() == 404) {
            return Optional.empty();
        }

        checkStatusOk(getSchemaResponse);

        return Optional.of(ByteBuffer.wrap(getSchemaResponse.body().bytes()));
    }

    private DownloadBean triggerExport() throws IOException {
        HttpUrl triggerExportUrl = HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("system")
            .addPathSegment("export")
            .addQueryParameter("download", "true")
            .build();

        Request triggerExportRequest = buildRequest(triggerExportUrl);
        Call exportCall = client.newCall(triggerExportRequest);
        Response exportResponse = exportCall.execute();

        checkStatusOk(exportResponse);

        return OBJECT_MAPPER.readValue(exportResponse.body().string(), DownloadBean.class);
    }

    private File downloadExportJson(DownloadBean downloadBean) throws IOException {
        LOGGER.info("Successfully triggered an export operation on the Manager API.");

        HttpUrl downloadUrl = HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("downloads")
            // ID from DownloadBean is ID path segment
            .addPathSegment(downloadBean.getId())
            .build();

        Request downloadRequest = buildRequest(downloadUrl);
        Call downloadCall = client.newCall(downloadRequest);
        Response downloadResponse = downloadCall.execute();

        checkStatusOk(downloadResponse);

        File downloadedFileTmp = File.createTempFile("EnrichPre21Export", ".json");

        // Get the underlying okio source, as it has some nice methods for writing directly without loads of copying.
        BufferedSource source = downloadResponse.body().source();
        BufferedSink buff = Okio.buffer(Okio.sink(downloadedFileTmp));
        buff.writeAll(source);
        buff.close();

        LOGGER.debug("Successfully downloaded an Apiman export to tmp: {}", downloadedFileTmp);

        return downloadedFileTmp;
    }

    private Request buildRequest(HttpUrl url) {
        String creds = Credentials.basic(username, password);

        return new Request.Builder()
            .url(url)
            // Setting here or OkHttp will wait for a 403 before trying authorization via #setAuthorisation.
            .header("Authorization", creds)
            .get()
            .build();
    }

    private void checkStatusOk(Response response) {
        if (response.code() / 100 != 2) {
            throw new IllegalStateException(
                "Remove server returned an unexpected status code: " + response.code()
            );
        }
    }
}
