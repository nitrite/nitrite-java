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

package org.dizitart.no2.sync.handlers;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.message.ErrorMessage;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class ErrorHandler implements MessageHandler<ErrorMessage> {
    private ReplicationTemplate replica;

    public ErrorHandler(ReplicationTemplate replica) {
        this.replica = replica;
    }

    @Override
    public void handleMessage(ErrorMessage message) {
        log.error("Received error message from server - {}", message.getError());
        replica.stopReplication(message.getError());
    }
}
