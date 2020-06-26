/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.sync.Replica;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;

import static org.dizitart.no2.collection.Document.createDocument;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class DataGateIntegrationTest {
    // TODO: Use https://www.testcontainers.org/ and use datagate-container for integration test

    static {
        try {
            InputStream inputStream = new FileInputStream("/home/anindya/codebase/nitrite/nitrite-java/nitrite-replication/src/test/resources/log4j2.xml");
            ConfigurationSource source = new ConfigurationSource(inputStream);
            Configurator.initialize(null, source);
        } catch (Exception ex) {
            // Handle here
        }
    }


    public static void main(String[] args) {
        Path dbPath = null;
        try {
            createUser();
            dbPath = Files.createTempFile("no2-datagate-it", "db");

            Nitrite db = NitriteBuilder.get()
                .filePath(dbPath.toFile())
                .openOrCreate();

            NitriteCollection collection = db.getCollection("datagateIntegration");
            Document document = createDocument().put("firstName", "Anindya")
                .put("lastName", "Chatterjee")
                .put("address", createDocument("street", "1234 Abcd Street")
                    .put("pin", 123456));
            collection.insert(document);

            String jwt = getToken();

            System.out.println("Token - " + jwt);
            Replica replica = Replica.builder()
                .of(collection)
                .remote("wss://127.0.0.1:3030/ws/datagate/anidotnet@gmail.com/datagateIntegration")
                .jwtAuth("anidotnet@gmail.com", jwt)
                .acceptAllCertificates(true)
                .create();

//            replica.subscribe(event -> {
//                if (event.getEventType() == ReplicationEventType.Stopped) {
//                    System.out.println("Reconnecting");
//                    replica.connect();
//                }
//            });

            replica.connect();
            System.out.println("Connected");
            Thread.sleep(10000);
            System.out.println("Completed");
            System.out.println("Collection Size - " + collection.size());
            for (Document d : collection.find()) {
                System.out.println(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dbPath != null) {
                    Files.delete(dbPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void createUser() throws Exception {
        OkHttpClient client = getUnsafeOkHttpClient();
        Request request = new Request.Builder()
            .url("https://127.0.0.1:3030/exists?email=anidotnet@gmail.com")
            .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        ObjectMapper mapper = new ObjectMapper();
        assert response.body() != null;
        JsonNode jsonNode = mapper.readValue(response.body().string(), JsonNode.class);
        if (jsonNode.has("exists")) {
            if (jsonNode.get("exists").asBoolean()) {
                return;
            }
        }

        String json = "{" +
            "\"email\":\"anidotnet@gmail.com\"," +
            "\"password\":\"chang3me\"," +
            "\"firstName\":\"Anindya\"," +
            "\"lastName\":\"Chatterjee\"," +
            "\"roles\": [\"admin\"]}";
        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"), json);

        request = new Request.Builder()
            .url("https://127.0.0.1:3030/register")
            .post(body)
            .build();

        call = client.newCall(request);
        response = call.execute();

        if (response.code() != 201) {
            throw new Exception("user creation failed");
        }
    }

    private static String getToken() throws Exception {
        OkHttpClient client = getUnsafeOkHttpClient();
        String json = "{" +
            "\"email\":\"anidotnet@gmail.com\"," +
            "\"password\":\"chang3me\"}";
        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"), json);

        Request request = new Request.Builder()
            .url("https://127.0.0.1:3030/login")
            .post(body)
            .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            assert response.body() != null;
            json = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readValue(json, JsonNode.class);

            if (jsonNode.has("token")) {
                return jsonNode.get("token").asText();
            }
        }

        throw new Exception("failed to login");
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                   String authType) throws
                        CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                   String authType) throws
                        CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
