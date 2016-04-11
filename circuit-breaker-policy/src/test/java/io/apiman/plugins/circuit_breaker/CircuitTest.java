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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author eric.wittmann@gmail.com
 */
public class CircuitTest {

    /**
     * Test method for {@link io.apiman.plugins.circuit_breaker.Circuit#reset()}.
     */
    @Test
    public void testCircuit_TripAndReset() throws InterruptedException {
        Circuit circuit = new Circuit(/*limit*/ 5, /*window*/ 1, /*reset*/ 2);
        for (int iterations = 0; iterations < 2; iterations++) {
            Assert.assertFalse(circuit.isBroken());
            circuit.addFault();
            Assert.assertFalse(circuit.isBroken());
            circuit.addFault();
            circuit.addFault();
            circuit.addFault();
            Assert.assertFalse(circuit.isBroken());
            // On the 5th fault, the circuit will trip
            circuit.addFault();
            Assert.assertTrue(circuit.isBroken());
            
            // Cannot be reset yet
            Assert.assertFalse(circuit.isResettable());
            Assert.assertFalse(circuit.startReset());
            
            Thread.sleep(1000);

            // Still cannot be reset!
            Assert.assertFalse(circuit.isResettable());
    
            Thread.sleep(1000);
            // NOW it can be reset
            Assert.assertTrue(circuit.isResettable());
            
            Assert.assertFalse(circuit.isResetting());
            Assert.assertTrue(circuit.startReset());
            Assert.assertTrue(circuit.isResetting());
            Assert.assertFalse(circuit.isResettable());
            
            Assert.assertTrue(circuit.isBroken());
            Assert.assertTrue(circuit.reset());
            Assert.assertFalse(circuit.isResetting());
            Assert.assertFalse(circuit.isResettable());
            Assert.assertFalse(circuit.isBroken());
       }
    }

    /**
     * Test method for {@link io.apiman.plugins.circuit_breaker.Circuit#reset()}.
     */
    @Test
    public void testCircuit_TripAndExtend() throws InterruptedException {
        Circuit circuit = new Circuit(/*limit*/ 5, /*window*/ 1, /*reset*/ 2);
        Assert.assertFalse(circuit.isBroken());
        circuit.addFault();
        Assert.assertFalse(circuit.isBroken());
        circuit.addFault();
        circuit.addFault();
        circuit.addFault();
        Assert.assertFalse(circuit.isBroken());
        // On the 5th fault, the circuit will trip
        circuit.addFault();
        Assert.assertTrue(circuit.isBroken());
        
        // Cannot be reset yet
        Assert.assertFalse(circuit.isResettable());
        
        Thread.sleep(1000);

        // Still cannot be reset!
        Assert.assertFalse(circuit.isResettable());

        Thread.sleep(1000);
        // NOW it can be reset
        Assert.assertTrue(circuit.isResettable());
        
        Assert.assertFalse(circuit.isResetting());
        circuit.startReset();
        Assert.assertTrue(circuit.isResetting());
        Assert.assertFalse(circuit.isResettable());
        
        // During the reset, get another fault, which will re-trip the circuit
        Assert.assertTrue(circuit.isBroken());
        circuit.addFault();
        Assert.assertFalse(circuit.isResetting());
        Assert.assertFalse(circuit.isResettable());
        Assert.assertTrue(circuit.isBroken());
        
        // Cannot be reset yet (need to wait an additional 2s, not just 1s)
        Thread.sleep(1000);
        Assert.assertFalse(circuit.isResettable());
        
        // Should be OK after another 1s
        Thread.sleep(1001);
        Assert.assertTrue(circuit.isResettable());
        Assert.assertTrue(circuit.startReset());
        Assert.assertTrue(circuit.reset());
    }

}
