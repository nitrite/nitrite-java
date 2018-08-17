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

package org.dizitart.no2.sync;

import org.dizitart.no2.sync.types.*;
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

