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

import io.apiman.manager.api.beans.metrics.ResponseStatsSummaryBean;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

/**
 * @author eric.wittmann@gmail.com
 */
public class ResponseStatsSummaryHandler implements ResultSetHandler<ResponseStatsSummaryBean> {
    
    /**
     * Constructor.
     */
    public ResponseStatsSummaryHandler() {
    }

    /**
     * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
     */
    @Override
    public ResponseStatsSummaryBean handle(ResultSet rs) throws SQLException {
        ResponseStatsSummaryBean rval = new ResponseStatsSummaryBean();
        while (rs.next()) {
            String rtype = rs.getString(1);
            long count = rs.getLong(2);
            if (rtype == null) {
                rval.setTotal(rval.getErrors() + rval.getFailures() + count);
            } else if (rtype.equals("failure")) { //$NON-NLS-1$
                rval.setTotal(rval.getTotal() + count);
                rval.setFailures(count);
            } else if (rtype.equals("error")) { //$NON-NLS-1$
                rval.setTotal(rval.getTotal() + count);
                rval.setErrors(count);
            }
        }
        return rval;
    }

}
