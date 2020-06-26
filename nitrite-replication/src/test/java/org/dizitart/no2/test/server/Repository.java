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

package org.dizitart.no2.test.server;

import lombok.Data;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;

import javax.websocket.Session;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Anindya Chatterjee
 */
@Data
public class Repository {
    private static Repository instance = new Repository();
    private Map<String, List<String>> collectionReplicaMap;
    private Map<String, List<String>> userReplicaMap;
    private Map<String, LastWriteWinMap> replicaStore;
    private Nitrite db;
    private String serverId;
    private Set<Session> authorizedSessions;
    private Map<String, String> userMap;
    private Long gcTtl;

    private Repository() {
        reset();
    }

    public static Repository getInstance() {
        return instance;
    }

    public void reset() {
        collectionReplicaMap = new ConcurrentHashMap<>();
        userReplicaMap = new ConcurrentHashMap<>();
        replicaStore = new ConcurrentHashMap<>();
        authorizedSessions = new HashSet<>();
        userMap = new ConcurrentHashMap<>();

        db = NitriteBuilder.get().openOrCreate();
        serverId = UUID.randomUUID().toString();
        gcTtl = 0L;
    }
}
