package org.dizitart.no2.store.tx;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class ChangeLog implements Comparable<ChangeLog> {
    private ChangeType changeType;
    private Object object;
    private Long timestamp;

    public ChangeLog() {
        timestamp = System.currentTimeMillis();
    }

    @Override
    public int compareTo(ChangeLog o) {
        if (o == null) return 1;
        return Long.compare(timestamp, o.timestamp);
    }
}
