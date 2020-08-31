package org.dizitart.no2.mapdb;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * MapDB's BTreeMap does not support null key. So this class acts as a surrogate for
 * null key.
 *
 * @author Anindya Chatterjee
 */
public class NullEntry implements Comparable<NullEntry>, Serializable {
    private static final long serialVersionUID = 1598819770L;
    private static final NullEntry instance = new NullEntry();

    private NullEntry() {
    }

    @Override
    public int compareTo(@NotNull NullEntry o) {
        return 0;
    }

    public static NullEntry getInstance() {
        return instance;
    }
}
