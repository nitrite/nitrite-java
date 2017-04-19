package org.dizitart.no2.event;

import org.dizitart.no2.NitriteContext;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * An abstract implementation of {@link EventBus}.
 *
 * @param <EventInfo>     the event information type parameter
 * @param <EventListener> the event listener type parameter
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public abstract class NitriteEventBus<EventInfo, EventListener>
        implements EventBus<EventInfo, EventListener> {

    private Set<EventListener> listeners;
    private ExecutorService eventExecutor;

    /**
     * Instantiates a new Nitrite event bus.
     *
     * @param context the context
     */
    public NitriteEventBus(NitriteContext context) {
        this.listeners = new HashSet<>();
        this.eventExecutor = context.getWorkerPool();
    }

    @Override
    public void register(EventListener eventListener) {
        if (eventListener != null) {
            listeners.add(eventListener);
        }
    }

    @Override
    public void deregister(EventListener eventListener) {
        if (eventListener != null) {
            listeners.remove(eventListener);
        }
    }

    @Override
    public void close() {
        listeners.clear();
    }

    /**
     * Gets the {@link ExecutorService} that executes listeners' code.
     *
     * @return the {@link ExecutorService}.
     */
    protected ExecutorService getEventExecutor() {
        return eventExecutor;
    }

    /**
     * Gets a set of all event listeners.
     *
     * @return the event listeners
     */
    protected Set<EventListener> getListeners() {
        return listeners;
    }
}
