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

/**
 * @author eric.wittmann@gmail.com
 */
public class CircuitFault {
    
    private final Date timestamp;
    
    /**
     * Constructor.
     */
    public CircuitFault() {
        this.timestamp = new Date();
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Returns true if the circuit fault falls within the given time range.
     * @param from
     * @param to
     */
    public boolean isInWindow(long from, long to) {
        long t = timestamp.getTime();
        return t <= to && t >= from;
    }

}
