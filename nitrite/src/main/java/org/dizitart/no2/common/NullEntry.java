package org.dizitart.no2.common;

import java.io.Serializable;

/**
 * This class acts as a surrogate for null key.
 *
 * @author Anindya Chatterjee
 */
public class NullEntry implements Comparable<NullEntry>, Serializable {
    private static final long serialVersionUID = 1598819770L;
    private static final NullEntry instance = new NullEntry();

    private NullEntry() {
    }

    @Override
    public int compareTo(NullEntry o) {
        return 0;
    }

    public static NullEntry getInstance() {
        return instance;
    }
}
