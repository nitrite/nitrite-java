package org.dizitart.no2.mapdb;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dizitart.no2.store.StoreConfig;
import org.dizitart.no2.store.events.StoreEventListener;
import org.mapdb.serializer.GroupSerializer;
import org.mapdb.volume.Volume;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Accessors(fluent = true)
public class MapDBConfig implements StoreConfig {
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Set<StoreEventListener> eventListeners;

    @Getter @Setter(AccessLevel.PACKAGE)
    private String filePath = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private MapDBStoreType storeType = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Volume volume = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Boolean volumeExists = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Long allocateStartSize = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Long allocateIncrement = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Boolean fileDeleteAfterClose = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Boolean fileDeleteAfterOpen = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Boolean isThreadSafe = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Integer concurrencyScale = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Boolean cleanerHack = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Boolean fileMmapPreclearDisable = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Long fileLockWait = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Boolean fileMmapfIfSupported = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Boolean closeOnJvmShutdown = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Boolean closeOnJvmShutdownWeakReference = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Boolean isReadOnly = false;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Boolean checksumStoreEnable = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Boolean checksumHeaderBypass = null;

    @Getter @Setter(AccessLevel.PACKAGE)
    private Map<Class<?>, GroupSerializer<?>> serializerRegistry = null;

    MapDBConfig() {
        eventListeners = new HashSet<>();
    }

    @Override
    public void addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
    }
}
