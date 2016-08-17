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
import io.vertx.ext.sql.ResultSet;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

import org.joda.time.DateTime;

/**
* @author Marc Savy {@literal <msavy@redhat.com>}
*/
public class VertxJdbcResultSet implements IJdbcResultSet {

    private ResultSet resultSet;
    private int row = -1;
    private List<JsonArray> rows;

    public VertxJdbcResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
        rows = resultSet.getResults();
    }
    
    /**
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
    public int getNumColumns() {
        return resultSet.getNumColumns();
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getRow()
     */
    @Override
    public int getRow() {
        return row;
    }

    protected int getNumRows() {
        return resultSet.getNumRows();
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#next()
     */
    @Override
    public boolean next() {
        boolean isok = hasNext();
        row += 1;
        return isok;
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#hasNext()
     */
    @Override
    public boolean hasNext() {
        return (row + 1) < getNumRows();
    }

    private void indexCheck() {
        if (row > (getNumRows()-1)) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("Row size: %d. Attempted invalid index: %d", getNumRows(), row + 1)); //$NON-NLS-1$
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
     * Looking at Vert.x source code this /should/ work.
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

    /**
     * Vert.x turns byte[] into a B64 encoded string. Reverse it.
     *
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#getBytes(int)
     */
    @Override
    public byte[] getBytes(int index) {
        String b64String = rows.get(row).getString(index);
        return Base64.getDecoder().decode(b64String);
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

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcResultSet#close()
     */
    @Override
    public void close() {
        // Nothing to do for vert.x
    }

}
