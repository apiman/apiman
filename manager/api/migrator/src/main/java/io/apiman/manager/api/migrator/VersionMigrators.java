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

import io.apiman.manager.api.migrator.vms.Version122FinalMigrator;

import java.util.ArrayList;
import java.util.List;

/**
 * All of the migrators - one for each version that requires migration (one for
 * each version for which a data model change was made).
 * @author eric.wittmann@gmail.com
 */
public class VersionMigrators {
    
    private static final List<Entry> migrators = new ArrayList<>();
    static {
        migrators.add(new Entry("1.2.2.Final", new Version122FinalMigrator())); //$NON-NLS-1$
    }

    /**
     * Gets the chain of migrators to use when migrating from version X to version Y.
     * @param fromVersion
     * @param toVersion
     */
    public static VersionMigratorChain chain(String fromVersion, String toVersion) {
        List<IVersionMigrator> matchedMigrators = new ArrayList<>();
        for (Entry entry : migrators) {
            if (entry.isBetween(fromVersion, toVersion)) {
                matchedMigrators.add(entry.migrator);
            }
        }
        return new VersionMigratorChain(matchedMigrators);
    }
    
    protected static class Entry {
        public final String version;
        public final IVersionMigrator migrator;
        
        /**
         * Constructor.
         * @param version
         * @param migrator
         */
        public Entry(String version, IVersionMigrator migrator) {
            this.version = version;
            this.migrator = migrator;
        }

        /**
         * Returns true if the version that this entry applies to lies in 
         * between the given two versions.  Note that 'from' is inclusive 
         * and 'to' is exclusive.
         * @param from
         * @param to
         */
        public boolean isBetween(String from, String to) {
            VersionComponents fromV = new VersionComponents(from);
            VersionComponents toV = new VersionComponents(to);
            VersionComponents v = new VersionComponents(version);
            
            return v.compareTo(fromV) >= 0 && v.compareTo(toV) < 0;
        }
    }
    
    private static class VersionComponents implements Comparable<VersionComponents> {
        public int major;
        public int minor;
        public int patch;
        
        /**
         * Constructor.
         * @param version
         */
        public VersionComponents(String version) {
            @SuppressWarnings("nls")
            String[] split = version.replace("-SNAPSHOT", "").split("\\.");
            major = new Integer(split[0]);
            minor = new Integer(split[1]);
            patch = new Integer(split[2]);
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(VersionComponents other) {
            if (major < other.major) {
                return -1;
            }
            if (major > other.major ) {
                return 1;
            }

            if (minor < other.minor) {
                return -1;
            }
            if (minor > other.minor ) {
                return 1;
            }

            if (patch < other.patch) {
                return -1;
            }
            if (patch > other.patch ) {
                return 1;
            }

            return 0;
        }
        
        
    }
}
