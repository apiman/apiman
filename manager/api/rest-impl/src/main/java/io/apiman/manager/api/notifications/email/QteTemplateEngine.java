package io.apiman.manager.api.notifications.email;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.qute.Engine;
import io.quarkus.qute.ReflectionValueResolver;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class QteTemplateEngine {
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(QteTemplateEngine.class);
    private final Engine engine;

    @Inject
    public QteTemplateEngine() {
        engine = Engine.builder()
                       .addDefaults()
                       .addValueResolver(new ReflectionValueResolver()) // Doesn't allow reflection by default
                       .build();
    }

    public Engine getEngine() {
        return engine;
    }

    public String applyTemplate(String rawTemplate, Map<String, Object> values) {
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
