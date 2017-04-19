package org.dizitart.no2.internals;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteContext;
import org.dizitart.no2.event.ChangeInfo;
import org.dizitart.no2.event.ChangeListener;
import org.dizitart.no2.event.NitriteEventBus;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class ChangeEventBus
        extends NitriteEventBus<ChangeInfo, ChangeListener> {

    ChangeEventBus(NitriteContext context) {
        super(context);
    }

    @Override
    public void post(final ChangeInfo changeInfo) {
        for (final ChangeListener listener : getListeners()) {
            String threadName = Thread.currentThread().getName();
            changeInfo.setOriginatingThread(threadName);

            getEventExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    listener.onChange(changeInfo);
                }
            });
        }
    }
}
