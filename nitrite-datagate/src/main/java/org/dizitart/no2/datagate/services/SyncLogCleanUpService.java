package org.dizitart.no2.datagate.services;

import lombok.extern.slf4j.Slf4j;
import org.jongo.Jongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.dizitart.no2.datagate.Constants.SYNC_LOG;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
@Service
public class SyncLogCleanUpService {

    @Autowired
    private Jongo jongo;

    @Value("${datagate.sync.log.cleanup.delay}")
    private int delay;

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    public void cleanUpSyncLog() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault()) ;
        LocalDate startDate = today.minusDays(delay);
        long delayMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        log.info("Deleting logs before " + startDate + " where epoch day = " + delayMillis);
        jongo.getCollection(SYNC_LOG).remove("{ epochDay: { $lte: # }}", delayMillis);
    }
}
