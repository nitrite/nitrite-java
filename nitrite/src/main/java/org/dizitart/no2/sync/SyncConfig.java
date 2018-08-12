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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.exceptions.ValidationException;

import java.lang.ref.WeakReference;

import static org.dizitart.no2.exceptions.ErrorMessage.SYNC_NO_REMOTE_COLLECTION;

/**
 * @author Anindya Chatterjee.
 */
class SyncConfig {
    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private ReplicationType replicationType = ReplicationType.BOTH_WAY;

    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private TimeSpan syncDelay;

    @Getter(AccessLevel.NONE) @Setter(AccessLevel.PUBLIC)
    private SyncTemplate syncTemplate;

    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private WeakReference<SyncEventListener> syncEventListener;

    SyncTemplate getSyncTemplate() {
        if (syncTemplate == null) {
            throw new ValidationException(SYNC_NO_REMOTE_COLLECTION);
        }
        return syncTemplate;
    }

    SyncEventListener getSyncEventListener() {
        if (syncEventListener != null && !syncEventListener.isEnqueued()) {
            return syncEventListener.get();
        }
        return null;
    }

    void setSyncEventListener(SyncEventListener syncEventListener) {
        this.syncEventListener = new WeakReference<>(syncEventListener);
    }
}
