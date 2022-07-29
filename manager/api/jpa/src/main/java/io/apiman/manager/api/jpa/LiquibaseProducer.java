/*
 * Copyright 2022 Scheer PAS Schweiz AG
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

import io.apiman.manager.api.core.config.ApiManagerConfig;

import java.sql.SQLException;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import liquibase.integration.cdi.CDILiquibaseConfig;
import liquibase.integration.cdi.annotations.LiquibaseType;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

/**
 * Sets up Liquibase patch applicator
 *
 * liquibase.should.run=false
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class LiquibaseProducer  {

    @Inject
    private ApiManagerConfig config;

    @Produces
    @LiquibaseType
    public CDILiquibaseConfig createConfig()  {
        CDILiquibaseConfig config = new CDILiquibaseConfig();
        config.setChangeLog("/liquibase/master.xml"); // TODO(msavy): could make this configurable?
        return config;
    }  

    @Produces
    @LiquibaseType
    public DataSource createDataSource() throws NamingException {
            DataSource ds;
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup(config.getHibernateDataSource());

            if (ds == null) {
                throw new RuntimeException("Datasource not found: " + config.getHibernateDataSource());
            }
            return ds;
    }  

    @Produces
    @LiquibaseType
    public ResourceAccessor create()  {
        return new ClassLoaderResourceAccessor(getClass().getClassLoader());
    }  

}
