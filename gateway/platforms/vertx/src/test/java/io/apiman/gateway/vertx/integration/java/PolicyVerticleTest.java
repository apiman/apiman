//package io.apiman.gateway.vertx.integration.java;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import io.apiman.gateway.engine.IConnectorFactory;
//import io.apiman.gateway.engine.IRegistry;
//import io.apiman.gateway.engine.impl.InMemoryRegistry;
//import io.apiman.gateway.engine.policy.IPolicyFactory;
//import io.apiman.gateway.engine.policy.PolicyFactoryImpl;
//import io.apiman.gateway.vertx.PolicyVerticle;
//import io.apiman.gateway.vertx.config.VertxEngineConfig;
//
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.vertx.java.core.AsyncResult;
//import org.vertx.java.core.AsyncResultHandler;
//import org.vertx.java.core.Handler;
//import org.vertx.java.core.eventbus.EventBus;
//import org.vertx.java.core.eventbus.Message;
//import org.vertx.testtools.TestVerticle;
//
//import static org.vertx.testtools.VertxAssert.*;
//
///**
// * @author Marc Savy <msavy@redhat.com>
// */
//public class PolicyVerticleTest extends TestVerticle {
//
//    private EventBus eb;
//    private VertxEngineConfig bVertxEngineConfig;
//    @Mock private Class<? extends IConnectorFactory> mConnectorFactory;
//
//    class TestablePolicyVerticle extends PolicyVerticle {
//        public void start() {
//            super.start();
//        }
//
//        protected VertxEngineConfig getEngineConfig() {
//            return bVertxEngineConfig;
//        }
//    }
//
//    public void setup() {
//        this.eb = vertx.eventBus();
//
//        // Sensible defaults for mVertxEngineConfig
//        bVertxEngineConfig = new VertxEngineConfig(container.config()) {
//            Map<String, String> emptyMap = new HashMap<>();
//
//            @Override
//            public Class<? extends IRegistry> getRegistryClass() {
//                return InMemoryRegistry.class;
//            }
//
//            @Override
//            public Map<String, String> getRegistryConfig() { return emptyMap; }
//
//            @Override
//            public Class<? extends IConnectorFactory> getConnectorFactoryClass() {
//                return mConnectorFactory;
//            }
//
//            @Override
//            public Map<String, String> getConnectorFactoryConfig() { return emptyMap; }
//
//            @Override
//            public Class<? extends IPolicyFactory> getPolicyFactoryClass() {
//                return PolicyFactoryImpl.class;
//            }
//
//            @Override
//            public Map<String, String> getPolicyFactoryConfig() { return emptyMap; }
//        };
//
//        eb.registerHandler(VertxEngineConfig.APIMAN_RT_EP_GATEWAY_REG_POLICY,
//                new Handler<Message<String>>() {
//
//            @Override
//            public void handle(Message<String> message) {
//                System.out.println(message.toString());
//            }
//
//        });
//    }
//
//    public void before() {
//        MockitoAnnotations.initMocks(this);
//    }
//
//    @Override
//    public void start() {
//        initialize();
//        setup();
//
//        container.deployVerticle(TestablePolicyVerticle.class.getCanonicalName(),
//                new AsyncResultHandler<String>() {
//
//            @Override
//            public void handle(AsyncResult<String> asyncResult) {
//                assertTrue(asyncResult.succeeded());
//                System.out.println(asyncResult.cause());
//
//                startTests();
//            }
//        });
//    }
//
//    @Test
//    public void testRequestResponse() {
//        before();
//    }
//}
