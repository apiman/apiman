/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.api.exportimport.json;

import io.apiman.manager.api.core.logging.IApimanLogger;
import io.apiman.manager.api.exportimport.manager.ExportImportConfigParser;
import io.apiman.manager.api.exportimport.read.IImportReader;
import io.apiman.manager.api.exportimport.write.IExportWriter;

/**
 * Factory for creating readers and writers for apiman export files.
 */
public interface IExportImportFactory {
    /**
     * Creates a reader based on the given config.  This should return a 
     * reader specific to the type of file being imported.
     * @param config
     * @param logger
     */
    IImportReader createReader(ExportImportConfigParser config, IApimanLogger logger);

    /**
     * Creates a writer based on the given config.  This should return a
     * writer specific to the type of output file being exported to.
     * @param config
     * @param logger
     */
    IExportWriter createWriter(ExportImportConfigParser config, IApimanLogger logger);
}
