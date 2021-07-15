package io.apiman.migration;

import io.apiman.migration.fix_pre_21.EnrichPre21ExportCommand;
import io.apiman.migration.util.LoggingMixin;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

/**
 * Export-related things will go here.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Command(name = "export", subcommands = EnrichPre21ExportCommand.class,
    description = "Perform an action against an Apiman export file")
public class ExportCommand implements Callable<Integer> {

    @Mixin
    LoggingMixin loggingMixin;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
