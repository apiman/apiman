/*
 * Copyright 2018 JBoss Inc
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

package io.apiman.common.es.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

/**
 * Wrapped {@link EmbeddedElastic}.
 *
 * When running in debug mode and hitting stop/terminate many IDEs don't execute shutdown hooks
 * (e.g. Eclipse).
 *
 * That means you can end up with a detached ES subprocess hanging around that needs killing manually.
 *
 * To work around this issue, this wrapper reflectively retrieves the ES process ID and writes it to a
 * file. If the server shuts down properly the ID is flushed out. Otherwise, on next run the PID
 * will be killed.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@SuppressWarnings("nls")
public class ApimanEmbeddedElastic {
    private final EmbeddedElastic elastic;
    private final Path pidPath;
    private long pid = -1;

    private ApimanEmbeddedElastic(EmbeddedElastic embeddedElastic, long port) {
        this.elastic = embeddedElastic;
        this.pidPath = Paths.get(System.getenv("HOME"), "/.cache/apiman/embedded-es-pid-" + port);
    }

    // Get version of ES that the project was built with. This is only really useful for
    // testing.
    public static String getEsBuildVersion() {
        URL url = ApimanEmbeddedElastic.class.getResource("apiman-embedded-elastic.properties");
        if (url == null) {
            throw new RuntimeException("embedded-elastic.properties missing.");
        } else {
            Properties allProperties = new Properties();
            try(InputStream is = url.openStream()){
                allProperties.load(is);
                return Optional
                        .ofNullable(allProperties.getProperty("apiman.embedded-es-version"))
                        .orElseThrow(() -> new RuntimeException("apiman.embedded-es-version"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public ApimanEmbeddedElastic start() throws IOException, InterruptedException {
        checkForDanglingProcesses();
        elastic.start();
        writeProcessId();
        return this;
    }

    public void stop() throws IOException {
        elastic.stop();
        deleteProcessId();
    }

    private void deleteProcessId() throws IOException {
        if (Files.exists(pidPath)) {
            List<String> pidLines = Files.readAllLines(pidPath).stream()
                    .filter(storedPid -> !storedPid.equalsIgnoreCase(String.valueOf(pid))) // Compare PID (long) with PID from file (String)
                    .collect(Collectors.toList());
            // Write back with successfully terminated PID removed.
            Files.write(pidPath, pidLines, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            System.err.println("No pid file. Did someone delete it while the program was running?");
        }
    }

    private void writeProcessId() throws IOException {
        try {
            // Create parent directory (i.e. ~/.cache/apiman/es-pid-{identifier})
            Files.createDirectories(pidPath.getParent());

            // Get the elasticServer instance variable
            Field elasticServerField = elastic.getClass().getDeclaredField("elasticServer");
            elasticServerField.setAccessible(true);
            Object elasticServerInstance = elasticServerField.get(elastic); // ElasticServer package-scoped so we can't get the real type.

            // Get the process ID (pid) long field from ElasticServer
            Field pidField = elasticServerInstance.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            pid = (int) pidField.get(elasticServerInstance); // Get the pid

            // Write to the PID file
            Files.write(pidPath, String.valueOf(pid).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    private void checkForDanglingProcesses() throws IOException {
        if (Files.exists(pidPath)) {
            for (String pid : Files.readAllLines(pidPath)) {
                System.err.println("Attempting to kill Elasticsearch process left over from previous execution: " + pid);
                Process result = Runtime.getRuntime().exec("kill " + pid);
                IOUtils.copy(result.getInputStream(), System.out);
                IOUtils.copy(result.getErrorStream(), System.err);
                result.destroy();
            }
            Files.deleteIfExists(pidPath);
        }
    }

    public static final class Builder {
        pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic.Builder wrappedBuilder;
        Integer port = -1;

        public Builder() {
            wrappedBuilder = pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic.builder();
        }

        public Builder withElasticVersion(String version) {
           wrappedBuilder.withElasticVersion(version);
           return this;
        }

        public Builder withDownloadDirectory(File dir) {
            wrappedBuilder.withDownloadDirectory(dir);
            return this;
        }

        public Builder withSetting(String clusterName, Object object) {
            wrappedBuilder.withSetting(clusterName, object);
            return this;
        }

        public Builder withCleanInstallationDirectoryOnStop(boolean b) {
            wrappedBuilder.withCleanInstallationDirectoryOnStop(b);
            return this;
        }

        public Builder withStartTimeout(int i, TimeUnit unit) {
            wrappedBuilder.withStartTimeout(i, unit);
            return this;
        }

        public ApimanEmbeddedElastic build() {
            if (port == -1) {
                throw new IllegalStateException("Must set port");
            }
            return new ApimanEmbeddedElastic(wrappedBuilder.build(), port);
        }

        public Builder withPort(Integer port) {
            wrappedBuilder.withSetting(PopularProperties.HTTP_PORT, port);
            this.port = port;
            return this;
        }

        public Builder withPort(String portRange) {
            wrappedBuilder.withSetting(PopularProperties.HTTP_PORT, portRange);
            if (portRange.contains("-")) {
                this.port = Integer.valueOf(portRange.split("-")[0].trim());
            } else {
                this.port = Integer.valueOf(portRange);
            }
            return this;
        }

        public Builder withDownloadUrl(URL url) {
            wrappedBuilder.withDownloadUrl(url);
            return this;
        }

        public Builder withInstallationDirectory(File installationDirectory) {
            wrappedBuilder.withInstallationDirectory(installationDirectory);
            return this;
        }

    }

}
