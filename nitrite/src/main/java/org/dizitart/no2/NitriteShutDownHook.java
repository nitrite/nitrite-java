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

package org.dizitart.no2;

import lombok.extern.slf4j.Slf4j;

/**
 * A JVM shutdown hook to close database properly before exiting for good.
 *
 * @author Anindya Chatterjee.
 */
@Slf4j
class NitriteShutDownHook extends Thread {
    private Nitrite db;

    NitriteShutDownHook(Nitrite db) {
        this.db = db;
    }

    @Override
    public void run() {
        if (db != null && !db.isClosed()) {
            try {
                db.close();
            } catch (Throwable t) {
                // close the db immediately and discards
                // any unsaved changes to avoid corruption
                log.error("Error while database shutdown", t);
                db.closeImmediately();
            }
        }
    }
}
