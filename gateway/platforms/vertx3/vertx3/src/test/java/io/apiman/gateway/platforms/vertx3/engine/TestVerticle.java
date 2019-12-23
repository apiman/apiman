package io.apiman.gateway.platforms.vertx3.engine;

import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

/**
 * Class used to access PluginRegistry in Unit test for {@link VertxPluginRegistry}.
 *
 * @author Jérémy HAURAY
 */
public class TestVerticle extends AbstractVerticle {

    private IEngine engine;


    public TestVerticle(VertxEngineConfig engineConfig) {

        vertx = Vertx.vertx();

        engine = new VertxConfigDrivenEngineFactory(vertx,engineConfig).createEngine();
    }

    public IPluginRegistry createPluginRegistry(){
        return engine.getPluginRegistry();
    }
}
