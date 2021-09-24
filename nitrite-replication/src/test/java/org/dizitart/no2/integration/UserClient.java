/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

/**
 * @author Anindya Chatterjee
 */
public class UserClient {

//    @Test
    public void test() throws Exception {
        createUser("127.0.0.1", 46005, "abcd@gmail.com");
    }

    public static void createUser(String host, Integer port, String user) throws Exception {
        OkHttpClient client = getUnsafeOkHttpClient();
        Request request = new Request.Builder()
            .url("http://" + host + ":" + port + "/exists?email=" + user)
            .build();

        Call call = client.newCall(request);
        Response response;
        try {
            response = call.execute();
            ObjectMapper mapper = new ObjectMapper();
            assert response.body() != null;
            JsonNode jsonNode = mapper.readValue(response.body().string(), JsonNode.class);
            if (jsonNode.has("exists")) {
                if (jsonNode.get("exists").asBoolean()) {
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("Error checking user " + user);
            e.printStackTrace();
            return;
        }

        String json = "{" +
            "\"email\":\"" + user + "\"," +
            "\"password\":\"chang3me\"," +
            "\"firstName\":\"Anindya\"," +
            "\"lastName\":\"Chatterjee\"," +
            "\"roles\": [\"admin\"]" +
            "}";

        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"), json);

        request = new Request.Builder()
            .url("http://" + host + ":" + port + "/register")
            .post(body)
            .build();

        call = client.newCall(request);
        response = call.execute();

        if (response.code() != 201) {
            throw new Exception("user creation failed");
        }
    }

    public static String getToken(String host, Integer port, String user) throws Exception {
        OkHttpClient client = getUnsafeOkHttpClient();
        String json = "{" +
            "\"email\":\"" + user + "\"," +
            "\"password\":\"chang3me\"}";


        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"), json);

        Request request = new Request.Builder()
            .url("http://" + host + ":" + port + "/login")
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

        System.err.println(response.body().string());
        throw new Exception("failed to login");
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
//            final TrustManager[] trustAllCerts = new TrustManager[]{
//                new X509TrustManager() {
//
//                    @Override
//                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
//                                                   String authType) {
//                    }
//
//                    @Override
//                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
//                                                   String authType) {
//                    }
//
//                    @Override
//                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                        return new java.security.cert.X509Certificate[]{};
//                    }
//                }
//            };
//
//            final SSLContext sslContext = SSLContext.getInstance("SSL");
//            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
//
//            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
//            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

//            builder.hostnameVerifier((hostname, session) -> true);
            builder.retryOnConnectionFailure(true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
