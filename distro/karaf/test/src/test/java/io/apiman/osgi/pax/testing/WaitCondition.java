package io.apiman.osgi.pax.testing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WaitCondition {
    private static final long WAIT_TIMEOUT_MILLIS = 10000;
    private static final Logger LOG = LoggerFactory.getLogger(WaitCondition.class);

    private String description;

    protected WaitCondition(String description) {
        this.description = description;
    }

    protected String getDescription() {
        return description;
    }

    protected abstract boolean isFulfilled() throws Exception;

    public void waitForCondition() throws InterruptedException {

        long startTime = System.currentTimeMillis();
        try {
            while (!isFulfilled() && System.currentTimeMillis() < startTime + WAIT_TIMEOUT_MILLIS) {
                Thread.sleep(100);
            }
            if (!isFulfilled()) {
                LOG.warn("Waited for {} for {} ms but condition is still not fulfilled", getDescription(), System.currentTimeMillis() - startTime);
            } else {
                LOG.info("Successfully waited for {} for {} ms", getDescription(), System.currentTimeMillis() - startTime);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error waiting for " + getDescription(), e);
        }
    }
}