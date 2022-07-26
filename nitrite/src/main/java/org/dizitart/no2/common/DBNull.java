package org.dizitart.no2.common;

import java.io.Serializable;

/**
 * This class acts as a surrogate for null key.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class DBNull extends DBValue implements Serializable {
    private static final long serialVersionUID = 1598819770L;
    private static final DBNull instance = new DBNull();

    private DBNull() {
        super(null);
    }

    @Override
    public int compareTo(DBValue o) {
        if (o == null || o instanceof DBNull) {
            return 0;
        }

        // null value always comes first
        return -1;
    }

    public static DBNull getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return null;
    }
}
