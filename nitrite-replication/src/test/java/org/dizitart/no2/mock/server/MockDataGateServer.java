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

package org.dizitart.no2.mock.server;


import org.glassfish.tyrus.server.Server;

/**
 * @author Anindya Chatterjee
 */
public class MockDataGateServer {
    private final int port;
    private Server server;
    private final MockRepository mockRepository;

    public MockDataGateServer(int port) {
        this.port = port;
        this.mockRepository = MockRepository.getInstance();
    }

    public void start() throws Exception {
        server = new Server("127.0.0.1", port, "", null, MockDataGateEndpoint.class);
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop()));
    }

    public void stop() {
        server.stop();
        mockRepository.reset();
    }
}