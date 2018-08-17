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
import org.dizitart.no2.Document;
import org.dizitart.no2.exceptions.SyncException;
import org.dizitart.no2.sync.types.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.util.ResponseUtils.errorResponse;

/**
 * Represents a template for DataGate sync operations.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
@Slf4j
public class DataGateSyncTemplate implements SyncTemplate {
    private static final MediaType JSON
        = MediaType.parse("application/json; charset=utf-8");

    private String serviceUrl;
    private String collection;
    private DataGateClient dataGateClient;
    private ObjectMapper objectMapper;

    /**
     * Instantiates a new {@link DataGateSyncTemplate}.
     *
     * @param dataGateClient the {@link DataGateClient}
     * @param collection     the remote collection name
     */
    public DataGateSyncTemplate(DataGateClient dataGateClient, String collection) {
        this.dataGateClient = dataGateClient;
        this.collection = collection;
        this.serviceUrl = "/datagate/api/v1/collection/" + collection;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ChangeFeed changedSince(FeedOptions feedOptions) {
        String url = dataGateClient.getServerBaseUrl() + serviceUrl + "/changedSince";
        OkHttpClient httpClient = dataGateClient.getHttpClient();
        Response response = null;
        try {
            String jsonOptions = objectMapper.writeValueAsString(feedOptions);
            Request request = new Request.Builder().url(url)
                    .post(RequestBody.create(JSON, jsonOptions)).build();
            response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new SyncException(errorMessage(errorResponse(response), SYE_CHANGE_SINCE_FAILED));
            }

            return objectMapper.readValue(response.body().bytes(),
                    ChangeFeed.class);
        } catch (Exception e) {
            log.error("Remote error while getting change feed from remote", e);
            throw new SyncException(SYNC_CHANGE_SINCE_REMOTE_ERROR, e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public boolean change(ChangeFeed changeFeed) {
        String url = dataGateClient.getServerBaseUrl() + serviceUrl + "/change";
        OkHttpClient httpClient = dataGateClient.getHttpClient();
        Response response = null;
        try {
            String jsonFeed = objectMapper.writeValueAsString(changeFeed);
            Request request = new Request.Builder().url(url)
                    .post(RequestBody.create(JSON, jsonFeed)).build();
            response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new SyncException(errorMessage(errorResponse(response), SYE_CHANGE_FAILED));
            }

            ChangeResponse changeResponse = objectMapper.readValue(response.body().bytes(),
                    ChangeResponse.class);
            return changeResponse.isChanged();
        } catch (Exception e) {
            log.error("Remote error while submitting change feed to remote", e);
            throw new SyncException(SYNC_CHANGE_REMOTE_ERROR, e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public List<Document> fetch(int offset, int limit) {
        String url = dataGateClient.getServerBaseUrl() + serviceUrl
            + "/fetch/offset/" + offset + "/limit/" + limit;
        OkHttpClient httpClient = dataGateClient.getHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new SyncException(errorMessage(errorResponse(response), SYE_FETCH_FAILED));
            }

            FetchResponse fetchResponse = objectMapper.readValue(response.body().bytes(),
                FetchResponse.class);
            return fetchResponse.getDocuments();
        } catch (Exception e) {
            log.error("Remote error while fetching document from remote", e);
            throw new SyncException(SYNC_FETCH_REMOTE_ERROR, e);
        }
    }

    @Override
    public long size() {
        String url = dataGateClient.getServerBaseUrl() + serviceUrl + "/size";
        OkHttpClient httpClient = dataGateClient.getHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new SyncException(errorMessage(errorResponse(response), SYE_SIZE_FAILED));
            }

            SizeResponse sizeResponse = objectMapper.readValue(response.body().bytes(),
                SizeResponse.class);
            return sizeResponse.getSize();
        } catch (Exception e) {
            log.error("Remote error while getting the size of the collection", e);
            throw new SyncException(SYNC_GET_SIZE_REMOTE_ERROR, e);
        }
    }

    @Override
    public void clear() {
        String url = dataGateClient.getServerBaseUrl() + serviceUrl + "/clear";
        OkHttpClient httpClient = dataGateClient.getHttpClient();
        Request request = new Request.Builder().url(url).delete().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new SyncException(errorMessage(errorResponse(response), SYE_CLEAR_FAILED));
            }
        } catch (SyncException se) {
            throw se;
        } catch (Exception e) {
            log.error("Remote error while getting change feed from remote", e);
            throw new SyncException(SYNC_CLEAR_REMOTE_ERROR, e);
        }
    }

    @Override
    public boolean isOnline() {
        String url = dataGateClient.getServerBaseUrl() + "/datagate/api/v1/ping";
        OkHttpClient httpClient = dataGateClient.getHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new SyncException(errorMessage(errorResponse(response), SYE_IS_ONLINE_FAILED));
            }

            OnlineResponse onlineResponse = objectMapper.readValue(response.body().bytes(),
                OnlineResponse.class);
            return onlineResponse.isOnline();
        } catch (Exception e) {
            log.error("Remote error while getting online status", e);
            return false;
        }
    }

    @Override
    public String getCollectionName() {
        return collection;
    }

    @Override
    public boolean trySyncLock(TimeSpan expiryDelay, String issuer) {
        long delay = TimeUnit.MILLISECONDS.convert(expiryDelay.getTime(),
            expiryDelay.getTimeUnit());

        String url = dataGateClient.getServerBaseUrl() + serviceUrl
                + "/tryLock/issuer/" + issuer + "/delay/" + delay;
        OkHttpClient httpClient = dataGateClient.getHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new SyncException(errorMessage(errorResponse(response), SYE_TRY_LOCK_FAILED));
            }

            TryLockResponse tryLockResponse = objectMapper.readValue(response.body().bytes(),
                    TryLockResponse.class);
            return tryLockResponse.isLockAcquired();
        } catch (Exception e) {
            log.error("Remote error while acquiring lock", e);
            throw new SyncException(SYNC_TRY_LOCK_REMOTE_ERROR, e);
        }
    }

    @Override
    public void releaseLock(String issuer) {
        String url = dataGateClient.getServerBaseUrl() + serviceUrl
                + "/releaseLock/issuer/" + issuer;
        OkHttpClient httpClient = dataGateClient.getHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new SyncException(errorMessage(errorResponse(response), SYE_RELEASE_LOCK_FAILED));
            }
        } catch (SyncException se) {
            throw se;
        } catch (Exception e) {
            log.error("Remote error while releasing lock from collection", e);
            throw new SyncException(SYNC_RELEASE_LOCK_REMOTE_ERROR, e);
        }
    }
}
