package org.dizitart.no2.datagate.models;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.dizitart.no2.sync.data.UserAgent;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * A wrapper object for sync operation log.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Data
public class SyncLog {
    private String issuer;
    private String collection;
    private UserAgent userAgent;
    private long lockAcquired;
    private long lockReleased;
    @Setter(AccessLevel.NONE)
    private long epochDay;

    public SyncLog() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault()) ;
        epochDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
