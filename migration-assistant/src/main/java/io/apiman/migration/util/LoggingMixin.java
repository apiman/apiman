package io.apiman.migration.util;

import io.apiman.migration.ApimanMigratorCli;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Spec.Target;

/**
 * Taken from https://github.com/remkop/picocli/blob/master/picocli-examples/src/main/java/picocli/examples/logging_mixin_advanced/LoggingMixin.java
 */
public class LoggingMixin {

    @Spec(Target.MIXEE)
    private CommandSpec mixee;

    private boolean[] verbosity = new boolean[0];

    private static LoggingMixin getTopLevelCommandLoggingMixin(CommandSpec commandSpec) {
        return ((ApimanMigratorCli) commandSpec.root().userObject()).loggingMixin;
    }

    @Option(names = {"-v", "--verbose"}, description = {
        "Specify multiple -v options to increase verbosity.",
        "For example, `-v -v -v` or `-vvv`"})
    public void setVerbose(boolean[] verbosity) {
        getTopLevelCommandLoggingMixin(mixee).verbosity = verbosity;
    }

    public boolean[] getVerbosity() {
        return getTopLevelCommandLoggingMixin(mixee).verbosity;
    }

    public static int executionStrategy(ParseResult parseResult) {
        getTopLevelCommandLoggingMixin(parseResult.commandSpec()).configureLoggers();
        return new CommandLine.RunLast().execute(parseResult);
    }

    public void configureLoggers() {
        Level level = getTopLevelCommandLoggingMixin(mixee).calcLogLevel();

        LoggerContext loggerContext = LoggerContext.getContext(false);
        LoggerConfig rootConfig = loggerContext.getConfiguration().getRootLogger();
        for (Appender appender : rootConfig.getAppenders().values()) {
            if (appender instanceof ConsoleAppender) {
                rootConfig.removeAppender(appender.getName());
                rootConfig.addAppender(appender, level, null);
            }
        }
        if (rootConfig.getLevel().isMoreSpecificThan(level)) {
            rootConfig.setLevel(level);
        }
        loggerContext.updateLoggers(); // apply the changes
    }

    private Level calcLogLevel() {
        switch (getVerbosity().length) {
            case 0:  return Level.WARN;
            case 1:  return Level.INFO;
            case 2:  return Level.DEBUG;
            default: return Level.TRACE;
        }
    }
}
