package org.dizitart.no2.common;

import java.io.Serializable;

/**
 * This class acts as a surrogate for null key.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class DBNull implements Comparable<DBNull>, Serializable {
    private static final long serialVersionUID = 1598819770L;
    private static final DBNull instance = new DBNull();

    private DBNull() {
    }

    @Override
    public int compareTo(DBNull o) {
        return 0;
    }

    public static DBNull getInstance() {
        return instance;
    }
}
