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

package org.dizitart.no2.datagate;

import org.dizitart.no2.datagate.models.SyncLog;
import org.dizitart.no2.sync.types.UserAgent;
import org.jongo.Jongo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;
import java.util.UUID;

import static org.dizitart.no2.datagate.Constants.SYNC_LOG;

/**
 * @author Anindya Chatterjee.
 */
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SyncLogDataGenerator {

    @Autowired
    private Jongo jongo;

    @Test
    public void testTime() {
        String[] devices = new String[] {"Android", "Windows", "iPhone", "Mac", "Linux", "Symbian", "Tizen"};
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            SyncLog syncLog = new SyncLog();
            syncLog.setIssuer(UUID.randomUUID().toString());

            UserAgent userAgent = new UserAgent();
            userAgent.setDevice(devices[random.nextInt(6)]);
            userAgent.setAppName("App" + random.nextInt(2));
            userAgent.setAppVersion(Integer.toString(random.nextInt(5)));
            userAgent.setClientId("Client" + random.nextInt(3));
            syncLog.setUserAgent(userAgent);
            jongo.getCollection(SYNC_LOG).insert(syncLog);
        }
    }
}
