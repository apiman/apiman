/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.plugins.circuit_breaker;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author eric.wittmann@gmail.com
 */
public class Circuit {
    
    private final int limit;
    private final int timeWindowMillis;
    private final int resetMillis;
    
    private boolean broken;
    private Set<CircuitFault> faults = new HashSet<>();
    private Date resetOn;
    private Date hardResetOn;
    
    private boolean resetting;
    
    /**
     * Constructor.
     * @param limit the # of faults that will trip the circuit
     * @param timeWindow the time window in seconds
     * @param reset the reset time in seconds
     */
    public Circuit(int limit, int timeWindow, int reset) {
        this.limit = limit;
        this.timeWindowMillis = timeWindow * 1000;
        this.resetMillis = reset * 1000;
    }

    /**
     * @return the broken
     */
    public boolean isBroken() {
        if (this.hardResetOn != null) {
            long now = System.currentTimeMillis();
            if (now >= this.hardResetOn.getTime()) {
                reset();
            }
        }
        return broken;
    }

    /**
     * Trip (open) the circuit.
     * @param resetMillis # of millis until the circuit can be reset
     */
    public void trip() {
        synchronized (faults) {
            this.resetOn = new Date(System.currentTimeMillis() + this.resetMillis);
            if (this.hardResetOn == null) {
                this.hardResetOn = new Date(System.currentTimeMillis() + this.resetMillis * 10);
            }
            this.broken = true;
            this.faults.clear();
            this.resetting = false;
        }
    }

    /**
     * Reset the circuit back to its original state.  Returns true if the reset
     * is successful.
     */
    public boolean reset() {
        if (this.resetting == false) {
            return false;
        }
        synchronized (faults) {
            faults.clear();
            broken = false;
            resetOn = null;
            hardResetOn = null;
            resetting = false;
            return true;
        }
    }

    /**
     * Adds a fault to the circuit.  
     */
    public void addFault() {
        synchronized (faults) {
            faults.add(new CircuitFault());
            filterFaults();
            if (faults.size() >= this.limit || isResetting()) {
                trip();
            }
        }
    }

    /**
     * Filter the faults based on the time window.
     */
    private void filterFaults() {
        long from = System.currentTimeMillis() - this.timeWindowMillis;
        long to = System.currentTimeMillis();
        Set<CircuitFault> toRemove = new HashSet<>();
        for (CircuitFault fault : faults) {
            if (!fault.isInWindow(from, to)) {
                toRemove.add(fault);
            }
        }
        faults.removeAll(toRemove);
    }

    /**
     * @return true if the circuit can be reset
     */
    public boolean isResettable() {
        if (resetting == true) {
            return false;
        } else if (!broken) {
            return false;
        } else {
            long now = System.currentTimeMillis();
            return now >= this.resetOn.getTime();
        }
    }

    /**
     * Called to start resetting the circuit.  Returns true if 
     */
    public boolean startReset() {
        if (!isResettable()) {
            return false;
        }
        resetting = true;
        return true;
    }

    /**
     * @return true if the circuit is currently being reset
     */
    public boolean isResetting() {
        return resetting;
    }

}
