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

package org.dizitart.no2.datagate.models;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.dizitart.no2.sync.types.UserAgent;

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
