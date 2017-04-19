package org.dizitart.no2.datagate;

import org.dizitart.no2.datagate.models.SyncLog;
import org.dizitart.no2.sync.data.UserAgent;
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
            syncLog.setUserAgent(userAgent);
            jongo.getCollection(SYNC_LOG).insert(syncLog);
        }
    }
}
