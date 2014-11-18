/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.vertx.worker;

import java.util.ArrayDeque;
import java.util.Deque;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.logging.Logger;

/**
 * Contains a queue of {@link Registrant} workers, which are collected by {@link #collectRegistrations()},
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public abstract class WorkerQueue<T extends Registrant> {
    protected Deque<T> policyWorkers;
    protected EventBus eb;
    protected Logger logger;
    protected String registrationTopic;
    protected Handler<Void> workerHandler;

    public WorkerQueue(String topic, EventBus eb, Logger logger) {
        this.registrationTopic = topic;
        this.policyWorkers = new ArrayDeque<>();
        this.eb = eb;
        this.logger = logger;
        
        collectRegistrations();
    }
    
    protected abstract void collectRegistrations();
    
    protected void workerHandler(Handler<Void> workerHandler) {
        this.workerHandler = workerHandler;
    }

    /**
     * @return Topic being listened to.
     */
    public String getTopic() {
        return registrationTopic;
    }
    
    /**
     * @return A worker if available, otherwise null.
     */
    public T poll() {
        return policyWorkers.pollFirst();
    }

    /**
     * Return a worker to the queue.
     * 
     * @param pinnedWorker The worker to return
     */
    public void add(T pinnedWorker) {
        policyWorkers.addLast(pinnedWorker);
    }
    
    /**
     * @return true if queue is empty; else false.
     */
    public boolean isEmpty() {
        return policyWorkers.isEmpty();
    }
}
