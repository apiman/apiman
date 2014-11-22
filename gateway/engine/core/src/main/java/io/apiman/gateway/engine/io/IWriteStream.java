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
package io.apiman.gateway.engine.io;


/**
 * Write into a stream by repeatedly submitting chunks via
 * {@link #write(IApimanBuffer)}. End of transmission is indicated by
 * {@link #end()}.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public interface IWriteStream extends IStream {
    /**
     * Write a chunk to the stream. No writes should be made after
     * {@link #end()} has been signalled.
     * 
     * @param chunk
     */
     void write(IApimanBuffer chunk);

    /**
     * Signal transmission has ended. This should only be called once, after
     * which no further calls to {@link #write(IApimanBuffer)} should be made.
     */
     void end();
}
