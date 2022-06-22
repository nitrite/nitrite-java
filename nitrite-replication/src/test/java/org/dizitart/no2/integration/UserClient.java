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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class UserClient {
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
            DataGateResponse<BooleanResponse> dataGateResponse = mapper.readValue(response.body().string(),
                new TypeReference<DataGateResponse<BooleanResponse>>() {});
            if (dataGateResponse.getData().getResult()) {
                return;
            }
        } catch (Exception e) {
            log.error("Error checking user " + user, e);
            return;
        }

        String json = "{" +
            "\"email\":\"" + user + "\"," +
            "\"password\":\"chang3me\"," +
            "\"firstName\":\"Anindya\"," +
            "\"lastName\":\"Chatterjee\"," +
            "\"roles\": [\"1\"]" +
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
            throw new Exception("User creation failed");
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
            DataGateResponse<Token> dataGateResponse = mapper.readValue(json,
                new TypeReference<DataGateResponse<Token>>() {});

            return dataGateResponse.getData().getToken();
        }

        throw new Exception("Failed to login");
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.retryOnConnectionFailure(true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
