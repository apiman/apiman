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

import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.exportimport.manager.ExportImportConfigParser;
import io.apiman.manager.api.exportimport.read.IImportReader;
import io.apiman.manager.api.exportimport.write.IExportWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings("nls")
public class JsonFileExportImportFactory implements IExportImportFactory {

    /**
     * @see io.apiman.manager.api.exportimport.json.IExportImportFactory#createWriter(io.apiman.manager.api.exportimport.manager.ExportImportConfigParser, IApimanLogger)
     */
    @Override
    public IExportWriter createWriter(ExportImportConfigParser config, IApimanLogger logger) {
        if (config.getJsonFile() == null)
            throw new IllegalArgumentException("Must provide path to JSON file to write");

        File outFile = new File(config.getJsonFile());

        if (outFile.exists() && !config.isOverwrite()) {
            throw new RuntimeException("File already exists: "+ outFile);
        } else if (outFile.exists()) {
            outFile.delete();
        }

        try {
            outFile.createNewFile();
            OutputStream os = new FileOutputStream(outFile);
            return new JsonExportWriter(os, logger);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @see io.apiman.manager.api.exportimport.json.IExportImportFactory#createReader(io.apiman.manager.api.exportimport.manager.ExportImportConfigParser, IApimanLogger)
     */
    @Override
    public IImportReader createReader(ExportImportConfigParser config, IApimanLogger logger) {
        if (config.getJsonFile() == null)
            throw new IllegalArgumentException("Must provide path to JSON file to read");

        File inFile = new File(config.getJsonFile());

        if (!inFile.exists())
            throw new IllegalArgumentException("JSON file does not exist " + inFile);

        try {
            InputStream is = new FileInputStream(inFile);
            return new JsonImportReader(logger, is);
        } catch (IOException e){
            throw new RuntimeException(e);
        }

    }

}
