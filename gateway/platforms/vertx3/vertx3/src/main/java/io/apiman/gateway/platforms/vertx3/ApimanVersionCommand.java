/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.gateway.platforms.vertx3;

import io.apiman.gateway.engine.Version;
import io.vertx.core.cli.CLIException;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.core.impl.launcher.commands.VersionCommand;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.launcher.DefaultCommand;

@Name("version")
@Summary("Displays the version.")
@Description("Prints the version of the apiman gateway.")
@SuppressWarnings("nls")
public class ApimanVersionCommand extends DefaultCommand {
    private static final Logger log = LoggerFactory.getLogger(ApimanVersionCommand.class);

    @Override
    public void run() throws CLIException {
        log.info("Apiman " + getApimanVersion());
        log.info("Vert.x " + VersionCommand.getVersion());
    }

    public static String getApimanVersion() {
        if (Version.get().getVersionString().contains("SNAPSHOT")) {
            return Version.get().getVersionString() + " " + Version.get().getVcsCommitDescription() + Version.get().getVersionDate();
        } else {
            return Version.get().getVersionString();
        }
    }
}
