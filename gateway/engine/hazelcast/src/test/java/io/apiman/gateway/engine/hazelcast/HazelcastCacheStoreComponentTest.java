package io.apiman.gateway.engine.hazelcast;

import com.hazelcast.config.*;
import io.apiman.gateway.engine.hazelcast.model.Example;
import io.apiman.gateway.engine.impl.ByteBufferFactoryComponent;
import io.apiman.gateway.engine.io.ByteBuffer;
import io.apiman.gateway.engine.io.ISignalReadStream;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for {@link HazelcastCacheStoreComponent}.
 *
 * @author Pete Cornish
 */
public class HazelcastCacheStoreComponentTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastCacheStoreComponentTest.class);

    private static HazelcastCacheStoreComponent member1;
    private static HazelcastCacheStoreComponent member2;
    private static Thread thread1;
    private static Thread thread2;
    private static volatile boolean shouldRun;
    private static CountDownLatch latch;

    @BeforeClass
    public static void setUp() throws Exception {
        shouldRun = true;
        latch = new CountDownLatch(2);

        startMemberThread("member1", 5701, 5702, (member, thread) -> {
            member1 = member;
            thread1 = thread;
        });

        startMemberThread("member2", 5702, 5701, (member, thread) -> {
            member2 = member;
            thread2 = thread;
        });

        // Instances are configured with a minimum cluster size of 2, so they will block on initialisation
        // until this condition is set.
        LOGGER.info("Waiting for quorum");
        latch.await();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException {
        LOGGER.info("Stopping Hazelcast instances");
        member1.reset();
        member2.reset();

        LOGGER.info("Waiting for threads to stop");
        shouldRun = false;
        thread1.join();
        thread2.join();
    }

    /**
     * Start a {@link HazelcastCacheStoreComponent} with its own Hazelcast instance in its own thread,
     * peered with the member on the given port.
     *
     * @param instanceName the unique name of this instance
     * @param memberPort   the port on which this member should listen
     * @param peerPort     the port of this member's peer in the group
     * @param callback     consumes the member and its thread
     */
    private static void startMemberThread(String instanceName, int memberPort, int peerPort,
                                   BiConsumer<HazelcastCacheStoreComponent, Thread> callback) {

        final Config config = new Config() {{
            setInstanceName(instanceName);
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

        final HazelcastCacheStoreComponent member = new HazelcastCacheStoreComponent(config);
        member.setBufferFactory(new ByteBufferFactoryComponent());

        final Thread memberThread = new Thread(() -> {
            // force the member to initialise, then signal when complete
            member.getMap();
            latch.countDown();

            // wait shutdown command
            while (shouldRun) Thread.yield();
        });

        callback.accept(member, memberThread);
        memberThread.start();
    }

    @Test
    public void putAndGetFromCluster() throws Exception {
        final String cacheKey = "cacheKeySimple";
        final Example cacheValue = new Example("cacheValue");

        member1.put(cacheKey, cacheValue, 120);
        member1.get(cacheKey, Example.class, result -> {
            assertFalse("Result should be retrieved successfully from initial member", result.isError());
            assertEquals(cacheValue, result.getResult());
        });

        // verify propagation
        member2.get(cacheKey, Example.class, result -> {
            assertFalse("Result should be retrieved successfully from peer member", result.isError());
            assertEquals(cacheValue, result.getResult());
        });
    }

    @Test
    public void putAndGetBinaryFromCluster() throws Exception {
        final String cacheKey = "cacheKeyBinary";
        final String cacheHead = "cacheHead";
        final String cacheBody = "cacheBody";

        final ISignalWriteStream writeStream = member1.putBinary(cacheKey, cacheHead, 120);
        writeStream.write(new ByteBuffer(cacheBody));
        writeStream.end();

        member1.getBinary(cacheKey, String.class, result -> {
            assertFalse("Result should be retrieved successfully from initial member", result.isError());

            final ISignalReadStream<String> readStream = result.getResult();
            readStream.bodyHandler(bodyResult -> assertEquals(cacheBody, new String(bodyResult.getBytes())));
            readStream.endHandler(endResult -> { /* no op */});
            readStream.transmit();

            assertEquals(cacheHead, readStream.getHead());
        });

        // verify propagation
            member2.getBinary(cacheKey, String.class, result -> {
                assertFalse("Result should be retrieved successfully from peer member", result.isError());

            final ISignalReadStream<String> readStream = result.getResult();
            readStream.bodyHandler(bodyResult -> assertEquals(cacheBody, new String(bodyResult.getBytes())));
            readStream.endHandler(endResult -> { /* no op */});
            readStream.transmit();

            assertEquals(cacheHead, readStream.getHead());
        });
    }
}

