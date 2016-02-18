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

import org.hibernate.dialect.Oracle10gDialect;

/**
 * A custom mysql dialect to convert BOOLEAN to BIT(1).  This is done automatically
 * in newer versions of hibernate.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class ApimanOracle12Dialect extends Oracle10gDialect {

    /**
     * Constructor.
     */
    public ApimanOracle12Dialect() {
    }
    
    /**
     * @see org.hibernate.dialect.Oracle9iDialect#registerCharacterTypeMappings()
     */
    @Override
    protected void registerCharacterTypeMappings() {
        super.registerCharacterTypeMappings();
        registerColumnType( Types.VARCHAR, "CLOB" );
    }
    
    /**
     * @see org.hibernate.dialect.Oracle8iDialect#registerLargeObjectTypeMappings()
     */
    @Override
    protected void registerLargeObjectTypeMappings() {
        super.registerLargeObjectTypeMappings();

        registerColumnType( Types.BINARY, "BLOB" );
        registerColumnType( Types.VARBINARY, "BLOB" );
        
        registerColumnType( Types.LONGVARCHAR, "CLOB" );
        registerColumnType( Types.LONGVARBINARY, "BLOB" );
    }

}
