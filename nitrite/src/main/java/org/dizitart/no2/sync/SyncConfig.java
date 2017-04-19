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
