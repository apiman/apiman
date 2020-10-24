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

package io.apiman.manager.api.migrator;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * As the data migrator's reader reads the data in the export file,
 * it will invoke methods on this handler interface.
 * @author eric.wittmann@gmail.com
 */
public interface IReaderHandler {
    
    public void onMetaData(ObjectNode node) throws IOException;

    public void onUser(ObjectNode node) throws IOException;
    
    public void onGateway(ObjectNode node) throws IOException;
    
    public void onPlugin(ObjectNode node) throws IOException;
    
    public void onRole(ObjectNode node) throws IOException;
    
    public void onPolicyDefinition(ObjectNode node) throws IOException;
    
    public void onOrg(ObjectNode node) throws IOException;

    void onDeveloper(ObjectNode node) throws IOException;

}
