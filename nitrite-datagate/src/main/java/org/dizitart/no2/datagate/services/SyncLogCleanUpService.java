/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
