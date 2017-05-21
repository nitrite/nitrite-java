package org.dizitart.no2.sync;

import org.dizitart.no2.sync.data.*;
import org.junit.Test;
import org.meanbean.lang.Factory;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

/**
 * @author Anindya Chatterjee
 */
public class DataClassTest {

    @Test
    public void testChangeFeed() {
        new BeanTester().testBean(ChangeFeed.class);
    }

    @Test
    public void testChangeResponse() {
        new BeanTester().testBean(ChangeResponse.class);
    }

    @Test
    public void testFeedOptions() {
        new BeanTester().testBean(FeedOptions.class);
    }

    @Test
    public void testFetchResponse() {
        new BeanTester().testBean(FetchResponse.class);
    }

    @Test
    public void testInfoResponse() {
        new BeanTester().testBean(InfoResponse.class);
    }

    @Test
    public void testOnlineResponse() {
        new BeanTester().testBean(OnlineResponse.class);
    }

    @Test
    public void testSizeResponse() {
        new BeanTester().testBean(SizeResponse.class);
    }

    @Test
    public void testTryLockResponse() {
        new BeanTester().testBean(TryLockResponse.class);
    }

    @Test
    public void testUserAccount() {
        Configuration configuration
                = new ConfigurationBuilder()
                .overrideFactory("authorities", new AuthoritiesFactory()).build();
        new BeanTester().testBean(UserAccount.class, configuration);
    }

    @Test
    public void testUserAgent() {
        new BeanTester().testBean(UserAgent.class);
    }

    class AuthoritiesFactory implements Factory<String[]> {
        @Override
        public String[] create() {
            return new String[] {"user", "admin", "client"};
        }
    }
}

