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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.dizitart.no2.exceptions.SyncException;
import org.dizitart.no2.sync.types.UserAccount;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.util.ResponseUtils.errorResponse;

/**
 * Represents a template for DataGate users operations.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
@Slf4j
public class DataGateUserTemplate {
    private static final MediaType JSON
        = MediaType.parse("application/json; charset=utf-8");
    private static final String userUrl = "/datagate/api/v1/user";

    private DataGateClient dataGateClient;
    private ObjectMapper objectMapper;

    /**
     * Instantiates a new {@link DataGateUserTemplate}.
     *
     * @param dataGateClient the {@link DataGateClient}
     */
    public DataGateUserTemplate(DataGateClient dataGateClient) {
        this.dataGateClient = dataGateClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Gets user account details by username.
     *
     * @param username the username
     * @return the user account details.
     */
    public UserAccount getUserAccount(String username) {
        String url = dataGateClient.getServerBaseUrl() + userUrl + "/" + username;
        OkHttpClient httpClient = dataGateClient.getHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new SyncException(errorMessage(errorResponse(response), SYE_GET_ACCOUNT_FAILED));
            }
            return objectMapper.readValue(response.body().bytes(), UserAccount.class);
        } catch (Exception e) {
            log.error("Remote error while getting user details", e);
            throw new SyncException(
                errorMessage("remote error while getting details for user " + username,
                    SYE_GET_ACCOUNT_REMOTE_ERROR), e);
        }
    }

    /**
     * Create a new user in the server.
     *
     * @param userAccount the user account details
     */
    public void createRemoteUser(UserAccount userAccount) {
        String url = dataGateClient.getServerBaseUrl() + userUrl + "/create";
        OkHttpClient httpClient = dataGateClient.getHttpClient();
        Response response = null;
        try {
            String jsonUser = objectMapper.writeValueAsString(userAccount);
            Request request = new Request.Builder().url(url)
                .post(RequestBody.create(JSON, jsonUser)).build();
            response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new SyncException(errorMessage(errorResponse(response), SYE_CREATE_ACCOUNT_FAILED));
            }
        } catch (SyncException se) {
            throw se;
        } catch (Exception e) {
            log.error("Remote error while creating new user", e);
            throw new SyncException(SYNC_ACCOUNT_CREATE_REMOTE_ERROR, e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Updates an existing remote user.
     *
     * @param userAccount the user account details
     */
    public void updateRemoteUser(UserAccount userAccount) {
        String url = dataGateClient.getServerBaseUrl() + userUrl + "/update";
        OkHttpClient httpClient = dataGateClient.getHttpClient();
        Response response = null;
        try {
            String jsonUser = objectMapper.writeValueAsString(userAccount);
            Request request = new Request.Builder().url(url)
                .put(RequestBody.create(JSON, jsonUser)).build();
            response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new SyncException(errorMessage(errorResponse(response), SYE_UPDATE_ACCOUNT_FAILED));
            }
        } catch (SyncException se) {
            throw se;
        } catch (Exception e) {
            log.error("Remote error while updating user", e);
            throw new SyncException(SYNC_ACCOUNT_UPDATE_REMOTE_ERROR, e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Deletes a remote user by username.
     *
     * @param username the username
     */
    public void deleteRemoteUser(String username) {
        String url = dataGateClient.getServerBaseUrl() + userUrl + "/delete/" + username;
        OkHttpClient httpClient = dataGateClient.getHttpClient();
        Request request = new Request.Builder().url(url).delete().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new SyncException(errorMessage(errorResponse(response), SYE_DELETE_ACCOUNT_FAILED));
            }
        } catch (SyncException se) {
            throw se;
        } catch (Exception e) {
            log.error("Remote error while deleting user", e);
            throw new SyncException(
                errorMessage("remote error while deleting user " + username,
                    SYE_DELETE_ACCOUNT_REMOTE_ERROR), e);
        }
    }
}
