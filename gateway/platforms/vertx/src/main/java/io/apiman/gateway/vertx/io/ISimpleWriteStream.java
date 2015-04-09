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
package io.apiman.gateway.vertx.io;

import org.vertx.java.core.buffer.Buffer;

/**
 * Interface representing simple write/end combination.
 * 
 * {@link #write(Buffer)} may be called an undefined number of times before {@link #end()} signals that
 * transmission has ended. No subsequent data should arrive after {@link #end()} has been signalled.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public interface ISimpleWriteStream {
    /**
     * Write a chunk
     * 
     * @param chunk the buffer data chunk
     */
    void write(Buffer chunk);
    
    /**
     * Finished writing chunks. No further calls to {@link #write(Buffer)} should occur.
     */
    void end();
}
