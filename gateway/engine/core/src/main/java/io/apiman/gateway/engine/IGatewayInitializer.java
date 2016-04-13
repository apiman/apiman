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

package io.apiman.gateway.engine;

/**
 * Provides a customizable mechanism for performing global initialization features
 * to the apiman gateway.  For example, an implementation of this interface might
 * initialize a RDBMS via a DDL.
 * 
 * @author eric.wittmann@gmail.com
 */
public interface IGatewayInitializer {
    
    /**
     * Called to do some sort of initialization prior to creating the apiman
     * gateway.
     */
    public void initialize();

}
