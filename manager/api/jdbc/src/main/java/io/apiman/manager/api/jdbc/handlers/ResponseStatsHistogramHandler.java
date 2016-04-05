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

import io.apiman.manager.api.beans.metrics.ResponseStatsDataPoint;
import io.apiman.manager.api.beans.metrics.ResponseStatsHistogramBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

/**
 * @author eric.wittmann@gmail.com
 */
public class ResponseStatsHistogramHandler implements ResultSetHandler<ResponseStatsHistogramBean> {
    
    private final ResponseStatsHistogramBean histogram;
    private final Map<Long, ResponseStatsDataPoint> index;

    /**
     * Constructor.
     * @param histogram
     * @param index
     */
    public ResponseStatsHistogramHandler(ResponseStatsHistogramBean histogram, Map<Long, ResponseStatsDataPoint> index) {
        this.histogram = histogram;
        this.index = index;
    }

    /**
     * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
     */
    @Override
    public ResponseStatsHistogramBean handle(ResultSet rs) throws SQLException {
        while (rs.next()) {
            long time = rs.getLong(1);
            String rtype = rs.getString(2);
            long count = rs.getLong(3);
            
            ResponseStatsDataPoint dataPoint = index.get(time);
            if (dataPoint != null) {
                if (rtype == null) {
                    dataPoint.setTotal(dataPoint.getErrors() + dataPoint.getFailures() + count);
                } else if (rtype.equals("failure")) { //$NON-NLS-1$
                    dataPoint.setTotal(dataPoint.getTotal() + count);
                    dataPoint.setFailures(count);
                } else if (rtype.equals("error")) { //$NON-NLS-1$
                    dataPoint.setTotal(dataPoint.getTotal() + count);
                    dataPoint.setErrors(count);
                }
            }
        }
        return histogram;
    }

}
