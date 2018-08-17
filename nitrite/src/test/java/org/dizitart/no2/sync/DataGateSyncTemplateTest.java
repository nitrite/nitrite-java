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
import org.dizitart.no2.Document;
import org.dizitart.no2.Retry;
import org.dizitart.no2.sync.types.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.sync.TimeSpan.timeSpan;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee.
 */
public class DataGateSyncTemplateTest {
    private ObjectMapper objectMapper;
    private MockWebServer server;
    private DataGateSyncTemplate dataGateSyncTemplate;

    @Rule
    public Retry retry = new Retry(3);

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        server = new MockWebServer();
        String url = server.url("").toString();
        DataGateClient client = new DataGateClient(url.replace("/", ""))
            .connectTimeout(100)
            .readTimeout(100)
            .withProxy(Proxy.NO_PROXY)
            .trustAllCerts()
            .withAuth("client", "client");
        dataGateSyncTemplate = new DataGateSyncTemplate(client, "test-collection");
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void testChangedSince() throws JsonProcessingException, InterruptedException {
        ChangeFeed feed = new ChangeFeed();
        feed.setModifiedDocuments(new ArrayList<Document>() {{ add(createDocument("abcd", "cdef")); }});
        feed.setOriginator("originator");
        feed.setRemovedDocuments(null);
        feed.setSequenceNumber(12244L);

        MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(objectMapper.writeValueAsString(feed));

        server.enqueue(response);

        ChangeFeed result = dataGateSyncTemplate.changedSince(new FeedOptions());
        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(recordedRequest.getRequestLine(),
            "POST /datagate/api/v1/collection/test-collection/changedSince HTTP/1.1");
        assertArrayEquals(result.getModifiedDocuments().toArray(), feed.getModifiedDocuments().toArray());
        assertArrayEquals(result.getRemovedDocuments().toArray(), feed.getRemovedDocuments().toArray());
        assertEquals(result.getSequenceNumber(), feed.getSequenceNumber());
        assertEquals(result.getOriginator(), feed.getOriginator());
    }

    @Test
    public void testChange() throws JsonProcessingException, InterruptedException {
        ChangeResponse changeResponse = new ChangeResponse();
        changeResponse.setChanged(true);

        MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(objectMapper.writeValueAsString(changeResponse));

        server.enqueue(response);

        boolean result = dataGateSyncTemplate.change(new ChangeFeed());
        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(recordedRequest.getRequestLine(),
            "POST /datagate/api/v1/collection/test-collection/change HTTP/1.1");
        assertEquals(result, changeResponse.isChanged());
    }

    @Test
    public void testFetch() throws JsonProcessingException, InterruptedException {
        FetchResponse fetchResponse = new FetchResponse();
        fetchResponse.setDocuments(new ArrayList<Document>() {{ add(createDocument("xyz", "123")); }});

        MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(objectMapper.writeValueAsString(fetchResponse));

        server.enqueue(response);

        List<Document> documentList = dataGateSyncTemplate.fetch(0, 5);
        assertEquals(documentList.size(), 1);
        assertEquals(documentList.get(0), documentList.get(0));

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(recordedRequest.getRequestLine(),
            "GET /datagate/api/v1/collection/test-collection/fetch/offset/0/limit/5 HTTP/1.1");
    }

    @Test
    public void testSize() throws JsonProcessingException, InterruptedException {
        SizeResponse sizeResponse = new SizeResponse();
        sizeResponse.setSize(3);

        MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(objectMapper.writeValueAsString(sizeResponse));

        server.enqueue(response);

        long size = dataGateSyncTemplate.size();
        assertEquals(size, sizeResponse.getSize());

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(recordedRequest.getRequestLine(),
            "GET /datagate/api/v1/collection/test-collection/size HTTP/1.1");
    }

    @Test
    public void testClear() throws InterruptedException {
        MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8");

        server.enqueue(response);
        dataGateSyncTemplate.clear();

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(recordedRequest.getRequestLine(),
            "DELETE /datagate/api/v1/collection/test-collection/clear HTTP/1.1");
    }

    @Test
    public void testIsOnline() throws JsonProcessingException, InterruptedException {
        OnlineResponse onlineResponse = new OnlineResponse();
        onlineResponse.setOnline(true);

        MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(objectMapper.writeValueAsString(onlineResponse));

        server.enqueue(response);

        boolean online = dataGateSyncTemplate.isOnline();
        assertTrue(online);

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(recordedRequest.getRequestLine(),
            "GET /datagate/api/v1/ping HTTP/1.1");
    }

    @Test
    public void testTryLock() throws JsonProcessingException, InterruptedException {
        TryLockResponse lockResponse = new TryLockResponse();
        lockResponse.setLockAcquired(true);

        MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(objectMapper.writeValueAsString(lockResponse));

        server.enqueue(response);

        boolean lock = dataGateSyncTemplate.trySyncLock(timeSpan(1, TimeUnit.SECONDS),
            "abcd");
        assertTrue(lock);

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(recordedRequest.getRequestLine(),
            "GET /datagate/api/v1/collection/test-collection/tryLock/issuer/abcd/delay/1000 HTTP/1.1");
    }

    @Test
    public void testReleaseLock() throws InterruptedException {
        MockResponse response = new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8");

        server.enqueue(response);
        dataGateSyncTemplate.releaseLock("abcd");

        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals(recordedRequest.getRequestLine(),
            "GET /datagate/api/v1/collection/test-collection/releaseLock/issuer/abcd HTTP/1.1");
    }
}
