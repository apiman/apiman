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

package io.apiman.gateway.engine.io;

import java.io.InputStream;

/**
 * Allows for reading and writing specific payload types.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IPayloadIO<T> {
    
    public T unmarshall(InputStream input) throws Exception;

    public T unmarshall(byte [] input) throws Exception;
    
    public byte [] marshall(T data) throws Exception;

}
