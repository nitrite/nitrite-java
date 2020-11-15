package org.dizitart.no2.store;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.store.events.EventInfo;
import org.dizitart.no2.store.events.StoreEventBus;
import org.dizitart.no2.store.events.StoreEventListener;
import org.dizitart.no2.store.events.StoreEvents;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.common.util.ObjectUtils.getKeyName;
import static org.dizitart.no2.common.util.ObjectUtils.getKeyedRepositoryType;

@Slf4j
public abstract class AbstractNitriteStore<Config extends StoreConfig>
    implements NitriteStore<Config> {

    @Getter @Setter
    private Config storeConfig;

    protected final NitriteEventBus<EventInfo, StoreEventListener> eventBus;
    protected NitriteConfig nitriteConfig;
    private IndexCatalog indexCatalog;

    protected AbstractNitriteStore() {
        eventBus = new StoreEventBus();
    }

    protected void alert(StoreEvents eventType) {
        EventInfo event = new EventInfo(eventType, nitriteConfig);
        eventBus.post(event);
    }

    @Override
    public Set<String> getCollectionNames() {
        NitriteMap<String, Document> catalogMap = openMap(COLLECTION_CATALOG, String.class, Document.class);
        Document document = catalogMap.get(TAG_COLLECTIONS);
        if (document == null) return new HashSet<>();

        return document.getFields();
    }

    @Override
    public Set<String> getRepositoryRegistry() {
        NitriteMap<String, Document> catalogMap = openMap(COLLECTION_CATALOG, String.class, Document.class);
        Document document = catalogMap.get(TAG_REPOSITORIES);
        if (document == null) return new HashSet<>();

        return document.getFields();
    }

    @Override
    public Map<String, Set<String>> getKeyedRepositoryRegistry() {
        NitriteMap<String, Document> catalogMap = openMap(COLLECTION_CATALOG, String.class, Document.class);
        Document document = catalogMap.get(TAG_KEYED_REPOSITORIES);
        if (document == null) return new HashMap<>();

        Map<String, Set<String>> resultMap = new HashMap<>();
        for (String field : document.getFields()) {
            String key = getKeyName(field);
            String type = getKeyedRepositoryType(field);

            Set<String> types;
            if (resultMap.containsKey(key)) {
                types = resultMap.get(key);
            } else {
                types = new HashSet<>();
            }
            types.add(type);
            resultMap.put(key, types);
        }
        return resultMap;
    }

    @Override
    public void beforeClose() {
        alert(StoreEvents.Closing);
    }

    @Override
    public IndexCatalog getIndexCatalog() {
        if (indexCatalog == null) {
            indexCatalog = new IndexCatalog(this);
        }
        return indexCatalog;
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
    }
}
