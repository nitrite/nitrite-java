package org.dizitart.no2.store;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.store.events.EventInfo;
import org.dizitart.no2.store.events.StoreEventBus;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;

import java.util.Map;
import java.util.Set;

/**
 * An abstract {@link NitriteStore} implementation.
 *
 * @param <Config> the type parameter
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Slf4j
public abstract class AbstractNitriteStore<Config extends StoreConfig>
    implements NitriteStore<Config> {

    @Getter @Setter
    private Config storeConfig;

    /**
     * The {@link NitriteEventBus} for the database.
     */
    protected final NitriteEventBus<EventInfo, StoreEventListener> eventBus;

    /**
     * The {@link NitriteConfig} for this store.
     */
    protected NitriteConfig nitriteConfig;

    private StoreCatalog storeCatalog;

    /**
     * Instantiates a new {@link AbstractNitriteStore}.
     */
    protected AbstractNitriteStore() {
        eventBus = new StoreEventBus();
    }

    /**
     * Alerts about an {@link StoreEvents} to all subscribed {@link StoreEventListener}s.
     *
     * @param eventType the event type
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
    public void removeRTree(String mapName) {
        this.removeMap(mapName);
    }

    @Override
    public void subscribe(StoreEventListener listener) {
        eventBus.register(listener);
    }

    @Override
    public void unsubscribe(StoreEventListener listener) {
        eventBus.deregister(listener);
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {
        this.nitriteConfig = nitriteConfig;
        this.storeCatalog = new StoreCatalog(this);
    }

    @Override
    public StoreCatalog getCatalog() {
        return storeCatalog;
    }
}
