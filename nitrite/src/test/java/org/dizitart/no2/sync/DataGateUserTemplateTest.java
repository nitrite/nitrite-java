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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.dizitart.no2.exceptions.SyncException;
import org.dizitart.no2.sync.types.UserAccount;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class DataGateUserTemplateTest {
    private ObjectMapper objectMapper;
    private MockWebServer server;
    private DataGateUserTemplate dataGateUserTemplate;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        server = new MockWebServer();
        String url = server.url("").toString();
        DataGateClient client = new DataGateClient(url.replace("/", ""))
                .connectTimeout(100)
                .readTimeout(100)
                .withAuth("client", "client");
        dataGateUserTemplate = new DataGateUserTemplate(client);
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void testGetUserDetails() throws IOException, InterruptedException {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserName("test");
        userAccount.setPassword("password");

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(objectMapper.writeValueAsString(userAccount));

        server.enqueue(response);

        UserAccount result = dataGateUserTemplate.getUserAccount("test");
        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(recordedRequest.getRequestLine(), "GET /datagate/api/v1/user/test HTTP/1.1");
        assertEquals(result, userAccount);
    }

    @Test
    public void testCreateRemoteUser() throws JsonProcessingException, InterruptedException {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserName("test");
        userAccount.setPassword("password");
        userAccount.setCollections(new ArrayList<String>() {{ add("collection"); }});
        userAccount.setAuthorities(new String[]{ "USER" });

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setResponseCode(201)
                .setBody("{}");

        server.enqueue(response);

        dataGateUserTemplate.createRemoteUser(userAccount);
        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(recordedRequest.getRequestLine(), "POST /datagate/api/v1/user/create HTTP/1.1");
    }

    @Test
    public void testUpdateRemoteUser() throws JsonProcessingException, InterruptedException {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserName("test");
        userAccount.setPassword("password");
        userAccount.setCollections(new ArrayList<String>() {{ add("collection"); }});
        userAccount.setAuthorities(new String[]{ "USER" });

        MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody("{}");

        server.enqueue(response);

        dataGateUserTemplate.updateRemoteUser(userAccount);
        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(recordedRequest.getRequestLine(), "PUT /datagate/api/v1/user/update HTTP/1.1");
    }

    @Test
    public void testDeleteRemoteUser() throws InterruptedException {
        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setResponseCode(200)
                .setBody("{}");
        server.enqueue(response);

        dataGateUserTemplate.deleteRemoteUser("test");
        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(recordedRequest.getRequestLine(), "DELETE /datagate/api/v1/user/delete/test HTTP/1.1");
    }

    @Test(expected = SyncException.class)
    public void testCreateRemoteUserFailed() throws InterruptedException {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserName("test");
        userAccount.setPassword("password");
        userAccount.setCollections(new ArrayList<String>() {{ add("collection"); }});
        userAccount.setAuthorities(new String[]{ "USER" });

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setResponseCode(401)
                .setBody("{}");

        server.enqueue(response);
        server.enqueue(response);

        dataGateUserTemplate.createRemoteUser(userAccount);
    }

    @Test(expected = SyncException.class)
    public void testUpdateRemoteUserFailed() throws InterruptedException {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserName("test");
        userAccount.setPassword("password");
        userAccount.setCollections(new ArrayList<String>() {{ add("collection"); }});
        userAccount.setAuthorities(new String[]{ "USER" });

        MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(401)
            .setBody("{}");

        server.enqueue(response);
        server.enqueue(response);

        dataGateUserTemplate.updateRemoteUser(userAccount);
    }
}
