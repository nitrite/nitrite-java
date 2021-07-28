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

package org.dizitart.no2.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Request;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.sync.event.ReplicationEventListener;
import org.dizitart.no2.sync.module.DocumentModule;

import java.math.BigInteger;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A builder api for creating a nitrite {@link Replica}.
 *
 * @author Anindya Chatterjee.
 * @since 4.0.0
 */
public class ReplicaBuilder {
    private NitriteCollection collection;
    private String datagateServerUrl;
    private String authToken;
    private TimeSpan timeout;
    private TimeSpan debounce;
    private Integer chunkSize;
    private String userName;
    private ObjectMapper objectMapper;
    private Proxy proxy;
    private boolean acceptAllCertificates = false;
    private final List<ReplicationEventListener> eventListeners;
    private String replicaName;

    /**
     * Instantiates a new {@link ReplicaBuilder}.
     */
    ReplicaBuilder() {
        chunkSize = 10;
        timeout = new TimeSpan(5, TimeUnit.SECONDS);
        debounce = new TimeSpan(1, TimeUnit.SECONDS);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new DocumentModule());
        eventListeners = new ArrayList<>();
    }

    /**
     * Creates a replica of a {@link NitriteCollection}.
     *
     * @param collection the collection
     * @return the replica builder
     */
    public ReplicaBuilder of(NitriteCollection collection) {
        this.collection = collection;
        return this;
    }

    /**
     * Creates a replica of an {@link ObjectRepository}.
     *
     * @param repository the repository
     * @return the replica builder
     */
    public ReplicaBuilder of(ObjectRepository<?> repository) {
        return of(repository.getDocumentCollection());
    }

    /**
     * Sets the remote datagate server url.
     *
     * @param datagateServerUrl the replication server
     * @return the replica builder
     */
    public ReplicaBuilder remote(String datagateServerUrl) {
        this.datagateServerUrl = datagateServerUrl;
        return this;
    }

    /**
     * Sets the JWT auth token and user name.
     *
     * @param userName  the user name
     * @param authToken the auth token
     * @return the replica builder
     */
    public ReplicaBuilder jwtAuth(String userName, String authToken) {
        this.authToken = authToken;
        this.userName = userName;
        return this;
    }

    /**
     * Sets the basic auth token.
     *
     * @param userName the user name
     * @param password the password
     * @return the replica builder
     */
    public ReplicaBuilder basicAuth(String userName, String password) {
        this.authToken = toHex(userName + ":" + password);
        this.userName = userName;
        return this;
    }

    /**
     * Sets the connection timeout.
     *
     * @param timeSpan the time span
     * @return the replica builder
     */
    public ReplicaBuilder timeout(TimeSpan timeSpan) {
        this.timeout = timeSpan;
        return this;
    }

    /**
     * Sets the chunk size of changes that will be transmitted.
     *
     * @param size the size
     * @return the replica builder
     */
    public ReplicaBuilder chunkSize(Integer size) {
        this.chunkSize = size;
        return this;
    }

    /**
     * Sets the debounce value.
     *
     * @param timeSpan the time span
     * @return the replica builder
     */
    public ReplicaBuilder debounce(TimeSpan timeSpan) {
        this.debounce = timeSpan;
        return this;
    }

    /**
     * Sets the {@link ObjectMapper} instance.
     *
     * @param objectMapper the object mapper
     * @return the replica builder
     */
    public ReplicaBuilder objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * Sets the proxy details.
     *
     * @param proxy the proxy
     * @return the replica builder
     */
    public ReplicaBuilder proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Sets a flag to accept all certificates.
     *
     * @param accept the accept
     * @return the replica builder
     */
    public ReplicaBuilder acceptAllCertificates(boolean accept) {
        this.acceptAllCertificates = accept;
        return this;
    }

    /**
     * Add a replication event listener to the replica.
     *
     * @param listener the listener
     * @return the replica builder
     */
    public ReplicaBuilder addReplicationEventListener(ReplicationEventListener listener) {
        this.eventListeners.add(listener);
        return this;
    }

    public ReplicaBuilder replicaName(String name) {
        this.replicaName = name;
        return this;
    }


    /**
     * Creates a {@link Replica}.
     *
     * @return the replica
     */
    public Replica create() {
        if (collection != null) {
            Request.Builder builder = createRequestBuilder();

            Config config = new Config();
            config.setCollection(collection);
            config.setChunkSize(chunkSize);
            config.setUserName(userName);
            config.setDebounce(getTimeoutInMillis(debounce));
            config.setObjectMapper(objectMapper);
            config.setTimeout(timeout);
            config.setRequestBuilder(builder);
            config.setProxy(proxy);
            config.setAcceptAllCertificates(acceptAllCertificates);
            config.setAuthToken(authToken);
            config.setEventListeners(eventListeners);
            config.setReplicaName(replicaName);

            ReplicatedCollection replicatedCollection = new ReplicatedCollection(config);
            return new Replica(config, replicatedCollection);
        } else {
            throw new ReplicationException("no collection or repository has been specified for replication", true);
        }
    }

    private Request.Builder createRequestBuilder() {
        Request.Builder builder = new Request.Builder();
        builder.url(datagateServerUrl);
        return builder;
    }

    private String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes(StandardCharsets.UTF_8)));
    }

    private int getTimeoutInMillis(TimeSpan connectTimeout) {
        return Math.toIntExact(connectTimeout.getTimeUnit().toMillis(connectTimeout.getTime()));
    }
}
