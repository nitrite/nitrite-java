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
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.sync.event.ReplicationEventListener;
import org.dizitart.no2.sync.module.DocumentModule;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * A builder api for creating a nitrite {@link Replica}.
 *
 * @author Anindya Chatterjee.
 * @since 4.0.0
 */
@Slf4j
public class ReplicaBuilder {
    private Nitrite db;
    private NitriteCollection collection;
    private String remoteHost;
    private Integer remotePort;
    private String authToken;
    private TimeSpan timeout;
    private TimeSpan pollingRate;
    private Integer chunkSize;
    private String tenant;
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
        remotePort = 46005;                                     // nitrite molar mass
        timeout = new TimeSpan(5, TimeUnit.SECONDS);
        pollingRate = new TimeSpan(1, TimeUnit.SECONDS);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new DocumentModule());
        eventListeners = new ArrayList<>();
    }

    /**
     * Creates a replica from a {@link Nitrite} database.
     *
     * @param db the db
     * @return the replica builder
     */
    public ReplicaBuilder database(Nitrite db) {
        this.db = db;
        return this;
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
     * Sets the remote datagate server host.
     *
     * @param remoteHost the replication server host
     * @return the replica builder
     */
    public ReplicaBuilder remoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
        return this;
    }

    /**
     * Sets the remote datagate server port.
     *
     * @param remotePort the replication server port
     * @return the replica builder
     */
    public ReplicaBuilder remotePort(Integer remotePort) {
        this.remotePort = remotePort;
        return this;
    }

    /**
     * Sets the remote datagate server tenant id.
     *
     * @param tenantId the replication server tenant id
     * @return the replica builder
     */
    public ReplicaBuilder tenant(String tenantId) {
        this.tenant = tenantId;
        return this;
    }

    /**
     * Sets the JWT auth token and username.
     *
     * @param userName  the username
     * @param authToken the auth token
     * @return the replica builder
     */
    public ReplicaBuilder jwtAuth(String userName, String authToken) {
        this.authToken = authToken;
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
     * Sets the polling rate value.
     *
     * @param timeSpan the time span
     * @return the replica builder
     */
    public ReplicaBuilder pollingRate(TimeSpan timeSpan) {
        this.pollingRate = timeSpan;
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
     * @param accept to accept
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

    /**
     * Sets an optional name for the replica.
     *
     * @param name the name of the replica
     * @return the replica builder
     */
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
        validateBuilder();
        Request.Builder builder = createRequestBuilder();

        Config config = new Config();
        config.setDb(db);
        config.setCollection(collection);
        config.setChunkSize(chunkSize);
        config.setUserName(userName);
        config.setTenant(tenant);
        config.setPollingRate(getTimeoutInMillis(pollingRate));
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
    }

    private Request.Builder createRequestBuilder() {
        String remoteUrl = String.format(Locale.getDefault(), "ws://%s:%d/ws/datagate/%s/%s/%s",
            remoteHost, remotePort, tenant, collection.getName(), userName);

        log.debug("Using remote datagate url " + remoteUrl);
        Request.Builder builder = new Request.Builder();
        builder.url(remoteUrl);
        return builder;
    }

    private int getTimeoutInMillis(TimeSpan connectTimeout) {
        return Math.toIntExact(connectTimeout.getTimeUnit().toMillis(connectTimeout.getTime()));
    }

    private void validateBuilder() {
        if (isNullOrEmpty(remoteHost)) {
            throw new ReplicationException("remote host is a mandatory field");
        }

        if (remotePort == null) {
            throw new ReplicationException("remote port is a mandatory field");
        }

        if (isNullOrEmpty(tenant)) {
            throw new ReplicationException("tenant id is a mandatory field");
        }

        if (db == null) {
            throw new ReplicationException("database is a mandatory field");
        }

        if (collection == null || isNullOrEmpty(collection.getName())) {
            throw new ReplicationException("collection or repository is a mandatory field");
        }

        if (isNullOrEmpty(userName)) {
            throw new ReplicationException("username is a mandatory field");
        }
    }
}
