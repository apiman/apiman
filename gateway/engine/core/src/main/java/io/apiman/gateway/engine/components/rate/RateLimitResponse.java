/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.gateway.engine.components.rate;

/**
 * A simple bean that is returned when using the rate limiter 
 * component.
 *
 * @author eric.wittmann@redhat.com
 */
public class RateLimitResponse {
    
    private boolean accepted;
    private int remaining;
    private long reset;
    
    /**
     * Constructor.
     */
    public RateLimitResponse() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @return the accepted
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * @param accepted the accepted to set
     */
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    /**
     * @return the remaining
     */
    public int getRemaining() {
        return remaining;
    }

    /**
     * @param remaining the remaining to set
     */
    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    /**
     * @return the reset
     */
    public long getReset() {
        return reset;
    }

    /**
     * @param reset the reset to set
     */
    public void setReset(long reset) {
        this.reset = reset;
    }

}
