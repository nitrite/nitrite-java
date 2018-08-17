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

import org.dizitart.no2.sync.types.UserAccount;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

import static org.dizitart.no2.datagate.Constants.*;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DataGateUserTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Jongo jongo;

    @Before
    public void setUp() {
        UserAccount clientAccount = new UserAccount();
        clientAccount.setUserName("wh_client");
        clientAccount.setPassword("wh_secret");
        clientAccount.setAuthorities(new String[] {AUTH_CLIENT});

        MongoCollection collection = jongo.getCollection(USER_REPO);
        collection.insert(clientAccount);
    }

    @After
    public void cleanUp() {
        MongoCollection collection = jongo.getCollection(USER_REPO);
        collection.remove("{userName: 'wh_client'}");
    }

    @Test
    public void testCreateUser() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserName("test");
        userAccount.setPassword("password");
        userAccount.setAuthorities(new String[] {AUTH_USER});
        userAccount.setCollections(new ArrayList<String>() {{ add("test-collection"); }});

        ResponseEntity<Void> responseEntity = restTemplate
                .withBasicAuth("wh_client", "wh_secret")
                .postForEntity("/api/v1/user/create", userAccount, Void.class);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getStatusCodeValue(), 200);

        MongoCollection collection = jongo.getCollection(USER_REPO);
        UserAccount account = collection.findOne("{userName: 'test'}").as(UserAccount.class);
        assertEquals(account, userAccount);

        collection.remove("{userName: 'test'}");
    }

    @Test
    public void testDeleteUser() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserName("test");
        userAccount.setPassword("password");
        userAccount.setAuthorities(new String[] {AUTH_USER});
        userAccount.setCollections(new ArrayList<String>() {{ add("test-collection"); }});

        ResponseEntity<Void> responseEntity = restTemplate
            .withBasicAuth("wh_client", "wh_secret")
            .postForEntity("/api/v1/user/create", userAccount, Void.class);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getStatusCodeValue(), 200);

        MongoCollection collection = jongo.getCollection(USER_REPO);
        UserAccount account = collection.findOne("{userName: 'test'}").as(UserAccount.class);
        assertEquals(account, userAccount);

        restTemplate.withBasicAuth("wh_client", "wh_secret")
            .delete("/api/v1/user/delete/test");

        collection = jongo.getCollection(USER_REPO);
        account = collection.findOne("{userName: 'test'}").as(UserAccount.class);
        assertNull(account);
    }

    @Test
    public void testWrongAuthorization() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserName("test");
        userAccount.setPassword("password");
        userAccount.setAuthorities(new String[] {AUTH_USER});
        userAccount.setCollections(new ArrayList<String>() {{ add("test-collection"); }});

        ResponseEntity<Void> responseEntity = restTemplate
            .withBasicAuth("client", "secret")
            .postForEntity("/api/v1/user/create", userAccount, Void.class);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getStatusCodeValue(), 401);
    }
}
