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
package io.apiman.manager.api.exportimport.manager;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.BooleanUtils;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
@ApplicationScoped
public class ExportImportConfigParser {
    public static final String APIMAN_ROOT = "apiman.migrate.";

    // apiman.migrate.function=import,export
    public static final String FUNCTION = APIMAN_ROOT + "function";
    public static final String OVERWRITE = APIMAN_ROOT + "overwrite";

    // apiman.migrate.provider=DEFAULT_PROVIDER
    public static final String PROVIDER = APIMAN_ROOT + "provider";
    public static final ExportImportProviderType DEFAULT_PROVIDER = ExportImportProviderType.JSON;

    // apiman.migrate.
    public static final String USER_STRATEGY = APIMAN_ROOT + "userStrategy";
    public static final ExportImportUserStrategy DEFAULT_USER_STRATEGY = ExportImportUserStrategy.ALL;

    // apiman.migrate.org=<ORG_NAME> else assume all orgs
    public static final String ORG_NAME = APIMAN_ROOT + "organization";

    // ZIP stuff
    public static final String ZIP_ROOT = APIMAN_ROOT + "zip.";
    public static final String ZIP_FILE = ZIP_ROOT + "file";
    public static final String ZIP_PASSWORD = ZIP_ROOT + "password";

    // JSON stuff
    public static final String JSON_ROOT = APIMAN_ROOT + "json.";
    public static final String JSON_FILE = JSON_ROOT + "file";
    public static final String JSON_DIR = JSON_ROOT + "dir";
    public static final String JSON_USERS_PER_FILE = JSON_ROOT + "usersPerFile";
    public static final String DEFAULT_JSON_USERS_PER_FILE = "5000";
    
    /**
     * Constructor.
     */
    public ExportImportConfigParser() {
    }

    public boolean isImportExport() {
        return getFunction() != ExportImportFunction.NONE;
    }

    public ExportImportProviderType getProvider() {
        return ExportImportProviderType.valueOf(System.getProperty(PROVIDER, DEFAULT_PROVIDER.name()));
    }

    public ExportImportUserStrategy getUserStrategy() {
        return ExportImportUserStrategy.valueOf(System.getProperty(USER_STRATEGY, DEFAULT_USER_STRATEGY.name()));
    }

    // apiman.migrate.organization=
    public String getOrgName() {
        return System.getProperty(ORG_NAME, null);
    }

    // apiman.migrate.function=
    public ExportImportFunction getFunction() {
        return ExportImportFunction.valueOf(System.getProperty(FUNCTION, "NONE"));
    }

    // apiman.migrate.zip.file=
    public String getZipFile() {
        return System.getProperty(ZIP_FILE);
    }

    // apiman.migrate.zip.password=
    public String getZipPassword() {
        return System.getProperty(ZIP_PASSWORD);
    }

    // apiman.migrate.json.file=
    public String getJsonFile() {
        return System.getProperty(JSON_FILE);
    }

    // apiman.migrate.overwrite=
    public boolean isOverwrite() {
        Boolean booleanObject = BooleanUtils.toBooleanObject(System.getProperty(OVERWRITE));
        if (booleanObject == null) {
            booleanObject = Boolean.FALSE;
        }
        return booleanObject;
    }
}
