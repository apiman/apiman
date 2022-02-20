package io.apiman.manager.api.notifications.email;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.core.config.ApiManagerConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.qute.Engine;
import io.quarkus.qute.ReflectionValueResolver;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class QteTemplateEngine {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(QteTemplateEngine.class);
    private final Engine engine;

    @Inject
    public QteTemplateEngine(ApiManagerConfig apimanConfig) {
        engine = Engine.builder()
                .addDefaults()
                .addValueResolver(new ReflectionValueResolver()) // Doesn't allow reflection by default
                .build();

        Path includesDir = apimanConfig.getConfigDirectory().resolve("notifications/email/tpl/includes");
        try (Stream<Path> files = Files.walk(includesDir, 10)) {
            files
                .filter(Files::isRegularFile)
                .filter(this::notHiddenFile)
                .forEach(f -> {
                    String relativeToBaseDir = includesDir.relativize(f).toString();
                    Template parsedTpl = engine.parse(readTemplate(f));
                    // Name of template is path relative to includes dir.
                    LOGGER.debug("Adding 'include' template: {0}", relativeToBaseDir);
                    engine.putTemplate(
                            relativeToBaseDir,
                            parsedTpl
                    );
                });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean notHiddenFile(Path p) {
        try {
            return !Files.isHidden(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String readTemplate(Path p) {
        try {
            return Files.readString(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Engine getEngine() {
        return engine;
    }

    public String applyTemplate(@NotNull String rawTemplate, @NotNull Map<String, Object> values) {
        if (Strings.isBlank(rawTemplate)) {
            return "";
        }
        LOGGER.trace("Applying template {0} with values {1}", rawTemplate, values);
        String result = doApplyTemplate(rawTemplate, values);
        LOGGER.trace("Rendered result: {0}", result);
        return result;
    }

    private String doApplyTemplate(String rawTemplate, Map<String, Object> values) {
        Template parsedTpl = engine.parse(rawTemplate);
        TemplateInstance tplInstance = parsedTpl.instance();
        values.forEach(tplInstance::data);
        return tplInstance.render();
    }
}
