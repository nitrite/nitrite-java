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

package org.dizitart.no2.sync.message;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Anindya Chatterjee.
 */
public enum MessageType {
    Error("no2.sync.error"),
    Connect("no2.sync.connect"),
    ConnectAck("no2.sync.connect.ack"),
    Disconnect("no2.sync.disconnect"),
    BatchChangeStart("no2.sync.batch.start"),
    BatchChangeContinue("no2.sync.batch.continue"),
    BatchChangeEnd("no2.sync.batch.end"),
    BatchAck("no2.sync.batch.ack"),
    BatchEndAck("no2.sync.batch.end.ack"),
    DataGateFeed("no2.sync.feed"),
    DataGateFeedAck("no2.sync.feed.ack");

    private String code;

    MessageType(String code) {
        this.code = code;
    }

    @JsonValue
    public String code() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }
}
