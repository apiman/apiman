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
package io.apiman.gateway.platforms.vertx3.components.jdbc;

import io.apiman.gateway.engine.components.jdbc.IJdbcResultSet;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.impl.actions.AbstractJDBCStatement;
import io.vertx.ext.sql.ResultSet;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;

/**
* @author Marc Savy {@literal <msavy@redhat.com>}
*/
public class VertxJdbcResultSet implements IJdbcResultSet {

    private ResultSet resultSet;
    private int row = 0;
    private List<JsonArray> rows;

    public VertxJdbcResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
        rows = resultSet.getResults();
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getColumnNames()
     */
    @Override
    public List<String> getColumnNames() {
        return resultSet.getColumnNames();
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getColumnSize()
     */
    @Override
    public int getColumnSize() {
        return resultSet.getNumColumns();
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getRow()
     */
    @Override
    public int getRow() {
        return row;
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getRowSize()
     */
    @Override
    public int getRowSize() {
        return resultSet.getNumRows();
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#next()
     */
    @Override
    public void next() {
        indexCheck();
        row += 1;
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#hasNext()
     */
    @Override
    public boolean hasNext() {
        return (row + 1) < getRowSize();
    }

    private void indexCheck() {
        if (!hasNext()) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("Row size: %d. Attempted invalid index: %d", getRowSize(), row + 1)); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getString(int)
     */
    @Override
    public String getString(int index) throws IndexOutOfBoundsException {
        indexCheck();
        return rows.get(row).getString(index);
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getShort(int)
     */
    @Override
    public Short getShort(int index) throws IndexOutOfBoundsException {
        indexCheck();
        return (short) rows.get(row).getInteger(index).intValue();
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getInteger(int)
     */
    @Override
    public Integer getInteger(int index) {
        indexCheck();
        return rows.get(row).getInteger(index);
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getLong(int)
     */
    @Override
    public Long getLong(int index) {
        indexCheck();
        return rows.get(row).getLong(index);
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getDouble(int)
     */
    @Override
    public Double getDouble(int index) {
        indexCheck();
        return rows.get(row).getDouble(index);
    }

    /**
     * Looking at Vert.x source code this /should/ work. See: {@link AbstractJDBCStatement#convertSqlValue}
     *
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getBigDecimal(int)
     */
    @Override
    public BigDecimal getBigDecimal(int index) {
        indexCheck();
        return (BigDecimal) rows.get(row).getValue(index);
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getBoolean(int)
     */
    @Override
    public Boolean getBoolean(int index) {
        return rows.get(row).getBoolean(index);
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getBytes(int)
     */
    @Override
    public byte[] getBytes(int index) {
        JsonArray array = rows.get(row).getJsonArray(index);
        byte[] result = new byte[array.size()];

        for (int i = 0; i < array.size(); i++) {
            result[i] = (byte) array.getValue(i);
        }
        return result;
    }

    /** (non-Javadoc)
     *
     * Should be OK as Vert.x uses ISO to turn timestamp, date, time, etc into a string.s
     *
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getDateTime(int)
     */
    @Override
    public DateTime getDateTime(int index) {
        return new DateTime(rows.get(row).getString(index));
    }


    @Override
    public boolean first() {
        if (resultSet.getNumRows() == 0)
            return false;
        row = 0;
        return true;
    }

    @Override
    public boolean last() {
        if (resultSet.getNumRows() == 0)
            return false;
        row = resultSet.getNumRows() - 1;
        return true;
    }

}
