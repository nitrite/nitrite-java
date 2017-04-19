package org.dizitart.no2.sync;

import org.dizitart.no2.NitriteContext;
import org.dizitart.no2.event.NitriteEventBus;

/**
 * @author Anindya Chatterjee
 */
class SyncEventBus
        extends NitriteEventBus<SyncEventData, SyncEventListener> {

    SyncEventBus(NitriteContext context) {
        super(context);
    }

    @Override
    public void post(final SyncEventData syncEventData) {
        for (final SyncEventListener listener : getListeners()) {
            getEventExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    listener.onSyncEvent(syncEventData);
                }
            });
        }
    }
}
