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

import io.apiman.common.logging.impl.StringBuilderLogger;
import io.apiman.manager.api.config.Version;
import io.apiman.test.common.json.JsonArrayOrderingType;
import io.apiman.test.common.json.JsonCompare;
import io.apiman.test.common.json.JsonMissingFieldType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("nls")
public class DataMigratorTest {
    
    /**
     * Test method for {@link io.apiman.manager.api.migrator.DataMigrator#migrate(java.io.File, java.io.File)}.
     */
    @Test
    public void testMigrate122to123() throws Exception {
        doTest( "export-files/export-from-1.2.2.Final.json",
                "export-files/export-from-1.2.2.Final_expected.json", 
                "1.2.3.Final");
    }

    private void doTest(String fileToMigrate, String expectedResultFile, String versionToMigrateTo) throws Exception {
        Version version = new Version();
        setVersion(version, versionToMigrateTo);
        StringBuilderLogger logger = new StringBuilderLogger();

        File tempOutputFile = File.createTempFile("_apiman", "tst");
        tempOutputFile.deleteOnExit();

        try {
            DataMigrator migrator = new DataMigrator();
            migrator.setLogger(logger);
            migrator.setVersion(version);
            
            InputStream input = getClass().getClassLoader().getResourceAsStream(fileToMigrate);
            OutputStream output = new FileOutputStream(tempOutputFile);
            
            // Migrate the data!
            migrator.migrate(input, output);
            
            JsonCompare compare = new JsonCompare();
            compare.setArrayOrdering(JsonArrayOrderingType.strict);
            compare.setIgnoreCase(false);
            compare.setMissingField(JsonMissingFieldType.fail);
            try ( FileInputStream actual = new FileInputStream(tempOutputFile);
                  InputStream expected = getClass().getClassLoader().getResourceAsStream(expectedResultFile);  ) {
                compare.assertJson(expected, actual);
            }
        } finally {
            FileUtils.deleteQuietly(tempOutputFile);
        }
    }

    /**
     * @param version
     * @param versionString
     * @throws Exception
     */
    private static void setVersion(Version version, String versionString) throws Exception {
        Field field = version.getClass().getDeclaredField("versionString");
        field.setAccessible(true);
        field.set(version, versionString);

        field = version.getClass().getDeclaredField("versionDate");
        field.setAccessible(true);
        field.set(version, new Date().toString());
    }

}
