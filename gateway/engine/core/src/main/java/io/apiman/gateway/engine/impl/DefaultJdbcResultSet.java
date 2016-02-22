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

package io.apiman.gateway.engine.impl;

import io.apiman.gateway.engine.components.jdbc.IJdbcResultSet;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

/**
 * A simple/default implementation of a {@link IJdbcResultSet}.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultJdbcResultSet implements IJdbcResultSet {
    
    private ResultSet resultSet;
    private List<String> columnNames;

    /**
     * Constructor.
     * @param resultSet the result set
     */
    public DefaultJdbcResultSet(ResultSet resultSet) throws Exception {
        this.resultSet = resultSet;
        columnNames = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int cols = metaData.getColumnCount();
        for (int i = 1; i <= cols; i++) {
          columnNames.add(metaData.getColumnLabel(i));
        }
    }
    
    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getColumnNames()
     */
    @Override
    public List<String> getColumnNames() {
        return columnNames;
    }
    
    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getNumColumns()
     */
    @Override
    public int getNumColumns() {
        return columnNames.size();
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getRow()
     */
    @Override
    public int getRow() {
        try {
            return resultSet.getRow();
        } catch (SQLException e) {
            return -1;
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#next()
     */
    @Override
    public boolean next() {
        try {
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#hasNext()
     */
    @Override
    public boolean hasNext() {
        try {
            return !resultSet.isLast();
        } catch (SQLException e) {
            return true;
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getString(int)
     */
    @Override
    public String getString(int index) throws IndexOutOfBoundsException {
        try {
            return resultSet.getString(index);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getShort(int)
     */
    @Override
    public Short getShort(int index) throws IndexOutOfBoundsException {
        try {
            return resultSet.getShort(index);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getInteger(int)
     */
    @Override
    public Integer getInteger(int index) {
        try {
            return resultSet.getInt(index);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getLong(int)
     */
    @Override
    public Long getLong(int index) {
        try {
            return resultSet.getLong(index);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getDouble(int)
     */
    @Override
    public Double getDouble(int index) {
        try {
            return resultSet.getDouble(index);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getBigDecimal(int)
     */
    @Override
    public BigDecimal getBigDecimal(int index) {
        try {
            return resultSet.getBigDecimal(index);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getBoolean(int)
     */
    @Override
    public Boolean getBoolean(int index) {
        try {
            return resultSet.getBoolean(index);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getBytes(int)
     */
    @Override
    public byte[] getBytes(int index) {
        try {
            return resultSet.getBytes(index);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getDateTime(int)
     */
    @Override
    public DateTime getDateTime(int index) {
        try {
            Date date = resultSet.getDate(index);
            return new DateTime(date);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#close()
     */
    @Override
    public void close() {
        try {
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
