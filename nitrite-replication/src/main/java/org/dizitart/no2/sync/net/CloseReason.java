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

package org.dizitart.no2.sync.net;

import lombok.Data;
import org.dizitart.no2.sync.ReplicationException;

/**
 * @author Anindya Chatterjee
 */
@Data
public class CloseReason {
    public static final CloseReason ServerClose = new CloseReason("Server Disconnected");
    public static final CloseReason ClientClose = new CloseReason("User Disconnected");

    private final String reason;
    private final Throwable error;

    public CloseReason(String reason) {
        this.reason = reason;
        this.error = null;
    }

    public CloseReason(String reason, Throwable error) {
        this.reason = reason;
        this.error = error;
    }

    public String getReasonMessage() {
        Throwable cause = error;
        while (true) {
            if (cause == null) {
                return reason;
            } else if (cause instanceof ReplicationException) {
                String message = cause.getMessage();
                while (true) {
                    if (message.length() > 123) {
                        message = message.substring(0, message.lastIndexOf(':'));
                    } else {
                        return message;
                    }
                }
            }
            cause = error.getCause();
        }
    }
}
