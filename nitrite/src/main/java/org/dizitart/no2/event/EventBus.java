package org.dizitart.no2.event;

/**
 * Represents a generic publish/subscribe event bus interface.
 *
 * @param <EventInfo>     the event information type parameter
 * @param <EventListener> the event listener type parameter
 * @author Anindya Chatterjee
 * @since 1.0
 */
public interface EventBus<EventInfo, EventListener> {
    /**
     * Registers an event listener to the event-bus.
     *
     * @param listener the event listener
     */
    void register(EventListener listener);

    /**
     * De-registers an already registered event listener.
     *
     * @param listener the event listener
     */
    void deregister(EventListener listener);

    /**
     * Posts an event to the event bus. All registered
     * event listeners for this event will receive the `eventInfo`
     * for further processing.
     * 
     * Event processing is asynchronous.
     *
     * @param eventInfo the event related information
     */
    void post(EventInfo eventInfo);

    /**
     * Closes the event bus and de-registers all event listeners.
     */
    void close();
}
