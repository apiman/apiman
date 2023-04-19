package io.apiman.manager.api.jpa;

import org.hibernate.dialect.SQLServer2012Dialect;

/**
 * Author: Marc Savy <marc@blackparrotlabs.io>
 */
public class ApimanMSSQLDialect extends SQLServer2012Dialect {

    /**
     * Constructor.
     */
    public ApimanMSSQLDialect() {
        super();
    }
}
