package io.apiman.gateway.engine.hazelcast;

import com.hazelcast.config.AwsConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.TcpIpConfig;
import io.apiman.gateway.engine.hazelcast.config.HazelcastInstanceManager;
import io.apiman.gateway.engine.hazelcast.model.Example;
import io.apiman.gateway.engine.impl.ByteBufferFactoryComponent;
import io.apiman.gateway.engine.io.ByteBuffer;
import io.apiman.gateway.engine.io.ISignalReadStream;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for {@link HazelcastCacheStoreComponent}.
 *
 * @author Pete Cornish
 */
public class HazelcastCacheStoreComponentTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastCacheStoreComponentTest.class);

    private static volatile boolean shouldRun;
    private static CountDownLatch latch;
    private static TestObjectHolder first, second;

    @BeforeClass
    public static void setUp() throws Exception {
        shouldRun = true;
        latch = new CountDownLatch(2);
        first = startMemberThread("member1", 5701, 5702);
        second = startMemberThread("member2", 5702, 5701);

        // Instances are configured with a minimum cluster size of 2, so they will block on initialisation
        // until this condition is set.
        LOGGER.info("Waiting for quorum");
        latch.await();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException {
        LOGGER.info("Stopping Hazelcast instances");
        first.manager.reset();
        second.manager.reset();

        LOGGER.info("Waiting for threads to stop");
        shouldRun = false;
        first.thread.join();
        second.thread.join();
    }

    /**
     * Start a {@link HazelcastCacheStoreComponent} with its own Hazelcast instance in its own thread,
     * peered with the member on the given port.
     *
     * @param name       the unique name of this instance
     * @param memberPort the port on which this member should listen
     * @param peerPort   the port of this member's peer in the group
     * @return TestObjectHolder      the instance, member and its thread
     */
    private static TestObjectHolder startMemberThread(String name, int memberPort, int peerPort) {
        final Config config = new Config() {{
            setInstanceName(name);
            getGroupConfig().setName("test-cluster");
            setProperty("hazelcast.initial.min.cluster.size", "2");

            getNetworkConfig().setPort(memberPort);
            getNetworkConfig().setJoin(new JoinConfig() {{
                setMulticastConfig(new MulticastConfig() {{
                    setEnabled(false);
                }});
                setTcpIpConfig(new TcpIpConfig() {{
                    setEnabled(true);
                    addMember("localhost:" + peerPort);
                }});
                setAwsConfig(new AwsConfig() {{
                    setEnabled(false);
                }});
            }});
        }};

        final HazelcastInstanceManager manager = new HazelcastInstanceManager();
        manager.setOverrideConfig(config);

        final Map<String, String> componentConfig = new HashMap<>();
        componentConfig.put(AbstractHazelcastComponent.CONFIG_EAGER_INIT, "false");

        final HazelcastCacheStoreComponent member = new HazelcastCacheStoreComponent(manager, componentConfig);
        member.setBufferFactory(new ByteBufferFactoryComponent());

        final Thread memberThread = new Thread(() -> {
            // force the member to initialise, then signal when complete
            member.getMap();
            latch.countDown();

            // await shutdown command
            while (shouldRun) Thread.yield();
        });

        memberThread.start();
        return new TestObjectHolder(manager, member, memberThread);
    }

    @Test
    public void putAndGetFromCluster() throws Exception {
        final String cacheKey = "cacheKeySimple";
        final Example cacheValue = new Example("cacheValue");

        first.cacheStore.put(cacheKey, cacheValue, 120);
        first.cacheStore.get(cacheKey, Example.class, result -> {
            assertFalse("Result should be retrieved successfully from initial member", result.isError());
            assertEquals(cacheValue, result.getResult());
        });

        // verify propagation
        second.cacheStore.get(cacheKey, Example.class, result -> {
            assertFalse("Result should be retrieved successfully from peer member", result.isError());
            assertEquals(cacheValue, result.getResult());
        });
    }

    @Test
    public void putAndGetBinaryFromCluster() throws Exception {
        final String cacheKey = "cacheKeyBinary";
        final String cacheHead = "cacheHead";
        final String cacheBody = "cacheBody";

        final ISignalWriteStream writeStream = first.cacheStore.putBinary(cacheKey, cacheHead, 120);
        writeStream.write(new ByteBuffer(cacheBody));
        writeStream.end();

        first.cacheStore.getBinary(cacheKey, String.class, result -> {
            assertFalse("Result should be retrieved successfully from initial member", result.isError());

            final ISignalReadStream<String> readStream = result.getResult();
            readStream.bodyHandler(bodyResult -> assertEquals(cacheBody, new String(bodyResult.getBytes())));
            readStream.endHandler(endResult -> { /* no op */});
            readStream.transmit();

            assertEquals(cacheHead, readStream.getHead());
        });

        // verify propagation
        second.cacheStore.getBinary(cacheKey, String.class, result -> {
            assertFalse("Result should be retrieved successfully from peer member", result.isError());

            final ISignalReadStream<String> readStream = result.getResult();
            readStream.bodyHandler(bodyResult -> assertEquals(cacheBody, new String(bodyResult.getBytes())));
            readStream.endHandler(endResult -> { /* no op */});
            readStream.transmit();

            assertEquals(cacheHead, readStream.getHead());
        });
    }

    private static class TestObjectHolder {
        HazelcastInstanceManager manager;
        HazelcastCacheStoreComponent cacheStore;
        Thread thread;

        TestObjectHolder(HazelcastInstanceManager manager, HazelcastCacheStoreComponent cacheStore, Thread thread) {
            this.manager = manager;
            this.cacheStore = cacheStore;
            this.thread = thread;
        }
    }
}
