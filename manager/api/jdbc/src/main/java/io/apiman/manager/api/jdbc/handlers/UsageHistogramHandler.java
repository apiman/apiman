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

import io.apiman.manager.api.beans.metrics.UsageDataPoint;
import io.apiman.manager.api.beans.metrics.UsageHistogramBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

/**
 * @author eric.wittmann@gmail.com
 */
public class UsageHistogramHandler implements ResultSetHandler<UsageHistogramBean> {
    
    private final UsageHistogramBean histogram;
    private final Map<Long, UsageDataPoint> index;

    /**
     * Constructor.
     * @param histogram
     * @param index
     */
    public UsageHistogramHandler(UsageHistogramBean histogram, Map<Long, UsageDataPoint> index) {
        this.histogram = histogram;
        this.index = index;
    }

    /**
     * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
     */
    @Override
    public UsageHistogramBean handle(ResultSet rs) throws SQLException {
        while (rs.next()) {
            long time = rs.getLong(1);
            long count = rs.getLong(2);
            UsageDataPoint dataPoint = index.get(time);
            if (dataPoint != null) {
                dataPoint.setCount(count);
            }
        }
        return histogram;
    }

}
