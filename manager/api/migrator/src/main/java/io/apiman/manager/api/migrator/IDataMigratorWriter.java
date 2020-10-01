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
 * @author eric.wittmann@gmail.com
 */
public interface IDataMigratorWriter extends AutoCloseable {

    public void writeMetaData(ObjectNode node) throws IOException;

    public void writeUser(ObjectNode node) throws IOException;
    
    public void writeGateway(ObjectNode node) throws IOException;
    
    public void writePlugin(ObjectNode node) throws IOException;
    
    public void writeRole(ObjectNode node) throws IOException;
    
    public void writePolicyDefinition(ObjectNode node) throws IOException;
    
    public void writeOrg(ObjectNode node) throws IOException;

    void writeDeveloper(ObjectNode node) throws IOException;

}
