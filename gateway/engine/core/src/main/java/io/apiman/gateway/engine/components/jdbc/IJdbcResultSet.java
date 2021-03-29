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
package io.apiman.gateway.engine.components.jdbc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Simple result set, akin to {@link java.sql.ResultSet}
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface IJdbcResultSet {

    /**
     * Get JDBC column names
     *
     * @return the list of column names
     */
    List<String> getColumnNames();

    /**
     * Get number of columns in set
     *
     * @return the number of columns
     */
    int getNumColumns();

    /**
     * Indicates which row presently pointed at
     *
     * @return the row number
     */
    int getRow();

    /**
     * Point at next row in result set
     * @return true if next
     */
    boolean next();

    /**
     * Indicates whether there is another row in the results
     *
     * @return true when another result row present, else false
     */
    boolean hasNext();

    /**
     * Get result as string
     *
     * @param index column offset
     * @return the result as a string
     * @throws IndexOutOfBoundsException when index is invalid
     */
    String getString(int index) throws IndexOutOfBoundsException;

    /**
     * Get result as a short
     *
     * @param index column offset
     * @return the result as a short
     * @throws IndexOutOfBoundsException when index is invalid
     */
    Short getShort(int index) throws IndexOutOfBoundsException;

    /**
     * Get result as an integer
     *
     * @param index column offset
     * @return the result as an integer
     * @throws IndexOutOfBoundsException when index is invalid
     */
    Integer getInteger(int index);

    /**
     * Get result as a long
     *
     * @param index column offset
     * @return the result as a long
     * @throws IndexOutOfBoundsException when index is invalid
     */
    Long getLong(int index);

    /**
     * Get result as a double
     *
     * @param index column offset
     * @return the result as a double
     * @throws IndexOutOfBoundsException when index is invalid
     */
    Double getDouble(int index);

    /**
     * Get result as a {@link BigDecimal}
     *
     * @param index column offset
     * @return the result as a BigDecimal
     * @throws IndexOutOfBoundsException when index is invalid
     */
    BigDecimal getBigDecimal(int index);

    /**
     * Get result as a boolean
     *
     * @param index column offset
     * @return the result as a boolean
     * @throws IndexOutOfBoundsException when index is invalid
     */
    Boolean getBoolean(int index);

    /**
     * Get result as a byte[]
     *
     * @param index column offset
     * @return the result as a byte[]
     * @throws IndexOutOfBoundsException when index is invalid
     */
    byte[] getBytes(int index);

    /**
     * Get result as a Date
     *
     * @param index column offset
     * @return the result as a Date
     * @throws IndexOutOfBoundsException when index is invalid
     */
    OffsetDateTime getDateTime(int index);

    /**
     * Close the result set
     */
    void close();
}
