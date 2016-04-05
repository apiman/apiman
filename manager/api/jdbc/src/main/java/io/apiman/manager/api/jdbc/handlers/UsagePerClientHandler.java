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

package io.apiman.manager.api.jdbc.handlers;

import io.apiman.manager.api.beans.metrics.UsagePerClientBean;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

/**
 * @author eric.wittmann@gmail.com
 */
public class UsagePerClientHandler implements ResultSetHandler<UsagePerClientBean> {
    
    /**
     * Constructor.
     */
    public UsagePerClientHandler() {
    }

    /**
     * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
     */
    @Override
    public UsagePerClientBean handle(ResultSet rs) throws SQLException {
        UsagePerClientBean rval = new UsagePerClientBean();
        while (rs.next()) {
            String clientId = rs.getString(1);
            long count = rs.getLong(2);
            rval.getData().put(clientId, count);
        }
        return rval;
    }

}
