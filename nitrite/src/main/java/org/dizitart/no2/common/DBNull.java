package org.dizitart.no2.common;

import lombok.Getter;

/**
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class DBNull extends DBValue {
    private static final long serialVersionUID = 1598819770L;
    @Getter
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

    @Override
    public String toString() {
        return null;
    }
}
