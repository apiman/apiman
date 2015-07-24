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
package io.apiman.manager.api.jpa;

import java.sql.Types;

import org.hibernate.dialect.H2Dialect;

/**
 * A custom h2 dialect to work around https://hibernate.atlassian.net/browse/HHH-9693
 *
 * @author eric.wittmann@redhat.com
 */
public class ApimanH2Dialect extends H2Dialect {

    /**
     * Constructor.
     */
    public ApimanH2Dialect() {
        registerColumnType(Types.LONGVARCHAR, String.format("varchar(%d)", Integer.MAX_VALUE)); //$NON-NLS-1$
    }

}
