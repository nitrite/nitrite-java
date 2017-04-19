package org.dizitart.no2.datagate;

import org.dizitart.no2.sync.data.UserAccount;
import org.dizitart.no2.sync.data.SizeResponse;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee.
 */
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DataGateSyncTest {
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
    }

    @After
    public void cleanUp() {
        restTemplate.withBasicAuth("wh_client", "wh_secret")
            .delete("/api/v1/user/delete/test");

        MongoCollection collection = jongo.getCollection(USER_REPO);
        collection.remove("{userName: 'wh_client'}");
    }

    @Test
    public void testWrongCredential() {
        ResponseEntity<SizeResponse> responseEntity = restTemplate
            .withBasicAuth("test", "password123")
            .getForEntity("/api/v1/collection/test-collection/size", SizeResponse.class);
        assertEquals(responseEntity.getStatusCodeValue(), 401);
    }

    @Test
    public void testWrongCollection() {
        ResponseEntity<SizeResponse> responseEntity = restTemplate
            .withBasicAuth("test", "password")
            .getForEntity("/api/v1/collection/test/size", SizeResponse.class);
        assertEquals(responseEntity.getStatusCodeValue(), 401);
    }
}
