package org.dizitart.no2.store;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.store.events.EventInfo;
import org.dizitart.no2.store.events.StoreEventBus;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;

import java.util.Map;
import java.util.Set;

/**
 * An abstract implementation of the {@link NitriteStore} interface
 * that provides common functionality for Nitrite data stores.
 *
 * @param <Config> the type of the store configuration object
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Getter
public abstract class AbstractNitriteStore<Config extends StoreConfig>
        implements NitriteStore<Config> {

    @Setter
    /**
     * The configuration object for the Nitrite store.
     */
    private Config storeConfig;

    /**
     * The event bus used to publish and subscribe to store events.
     */
    protected final NitriteEventBus<EventInfo, StoreEventListener> eventBus;

    /**
     * The NitriteConfig object that holds the configuration for the Nitrite
     * database.
     */
    protected NitriteConfig nitriteConfig;

    /**
     * The catalog of all the collections and repositories in the Nitrite database.
     */
    private StoreCatalog storeCatalog;

    /**
     * Instantiates a new {@link AbstractNitriteStore}.
     */
    protected AbstractNitriteStore() {
        eventBus = new StoreEventBus();
    }

    /**
     * Sends an alert to the event bus for the specified event type.
     *
     * @param eventType the type of event to send an alert for
     */
    protected void alert(StoreEvents eventType) {
        EventInfo event = new EventInfo(eventType, nitriteConfig);
        eventBus.post(event);
    }

    @Override
    public Set<String> getCollectionNames() {
        return getCatalog().getCollectionNames();
    }

    @Override
    public Set<String> getRepositoryRegistry() {
        return getCatalog().getRepositoryNames();
    }

    @Override
    public Map<String, Set<String>> getKeyedRepositoryRegistry() {
        return getCatalog().getKeyedRepositoryNames();
    }

    @Override
    public void beforeClose() {
        alert(StoreEvents.Closing);
    }

    @Override
    public String subscribe(StoreEventListener listener) {
        return eventBus.register(listener);
    }

    @Override
    public void unsubscribe(String subscription) {
        eventBus.deregister(subscription);
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
        this.nitriteConfig = nitriteConfig;
    }

    @Override
    public StoreCatalog getCatalog() {
        if (storeCatalog == null) {
            this.storeCatalog = new StoreCatalog(this);
        }
        return storeCatalog;
    }
}
