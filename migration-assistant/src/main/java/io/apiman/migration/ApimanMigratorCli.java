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

package io.apiman.migration;

import io.apiman.migration.util.LoggingMixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

/**
 * Base class, nothing much here yet, but gives us space to populate with new subcommands.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */

@Command(name = "migration-helper", subcommands = {ExportCommand.class, CommandLine.HelpCommand.class },
    description = "A tool to help with migration-related tasks that Apiman can't do by itself")
public class ApimanMigratorCli implements Runnable {
    static final Logger LOGGER = LogManager.getLogger("migration-helper");

    @Mixin public LoggingMixin loggingMixin;

    public void run() {
        LOGGER.trace("migration-helper starting");
    }

    public static void main(String... args) {
        ApimanMigratorCli migratorCli = new ApimanMigratorCli();

        try {
            int exitCode = new CommandLine(migratorCli)
                .setExecutionStrategy(LoggingMixin::executionStrategy)
                .execute(args);

            System.exit(exitCode);
        } catch (Exception e) {
            if (migratorCli.loggingMixin.getVerbosity().length > 1) {
                e.printStackTrace();
            }
            LOGGER.error("An error occurred: {}", e.getMessage());
        }
    }
}
