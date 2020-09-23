package org.dizitart.no2.mapdb;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dizitart.no2.store.events.StoreEventListener;
import org.mapdb.serializer.GroupSerializer;
import org.mapdb.volume.Volume;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Getter
@Setter
@Accessors(fluent = true)
public class MapDBModuleBuilder {
    private String filePath;
    private MapDBStoreType storeType = null;
    private Volume volume = null;
    private Boolean volumeExists = null;
    private Long allocateStartSize = null;
    private Long allocateIncrement = null;
    private Boolean fileDeleteAfterClose = null;
    private Boolean fileDeleteAfterOpen = null;
    private Boolean isThreadSafe = null;
    private Integer concurrencyScale = null;
    private Boolean cleanerHack = null;
    private Boolean fileMmapPreclearDisable = null;
    private Long fileLockWait = null;
    private Boolean fileMmapfIfSupported = null;
    private Boolean closeOnJvmShutdown = null;
    private Boolean closeOnJvmShutdownWeakReference = null;
    private Boolean readOnly = false;
    private Boolean checksumStoreEnable = null;
    private Boolean checksumHeaderBypass = null;

    private MapDBConfig dbConfig;

    @Setter(AccessLevel.NONE)
    private final Set<StoreEventListener> eventListeners;

    @Setter(AccessLevel.NONE)
    private final Map<Class<?>, GroupSerializer<?>> serializerRegistry;

    MapDBModuleBuilder() {
        dbConfig = new MapDBConfig();
        eventListeners = new HashSet<>();
        serializerRegistry = new HashMap<>();
    }

    public MapDBModuleBuilder filePath(File file) {
        if (file != null) {
            this.filePath = file.getPath();
        }
        return this;
    }

    public MapDBModuleBuilder filePath(String path) {
        this.filePath = path;
        return this;
    }

    public MapDBModuleBuilder addStoreEventListener(StoreEventListener listener) {
        eventListeners.add(listener);
        return this;
    }

    public void registerSerializer(Class<?> type, GroupSerializer<?> serializer) {
        serializerRegistry.put(type, serializer);
    }

    public MapDBModule build() {
        MapDBModule module = new MapDBModule(filePath());

        dbConfig.filePath(filePath());
        dbConfig.storeType(storeType());
        dbConfig.volume(volume());
        dbConfig.volumeExists(volumeExists());
        dbConfig.allocateStartSize(allocateStartSize());
        dbConfig.allocateIncrement(allocateIncrement());
        dbConfig.fileDeleteAfterClose(fileDeleteAfterClose());
        dbConfig.fileDeleteAfterOpen(fileDeleteAfterOpen());
        dbConfig.isThreadSafe(isThreadSafe());
        dbConfig.concurrencyScale(concurrencyScale());
        dbConfig.cleanerHack(cleanerHack());
        dbConfig.fileMmapPreclearDisable(fileMmapPreclearDisable());
        dbConfig.fileLockWait(fileLockWait());
        dbConfig.fileMmapfIfSupported(fileMmapfIfSupported());
        dbConfig.closeOnJvmShutdown(closeOnJvmShutdown());
        dbConfig.closeOnJvmShutdownWeakReference(closeOnJvmShutdownWeakReference());
        dbConfig.isReadOnly(readOnly());
        dbConfig.checksumStoreEnable(checksumStoreEnable());
        dbConfig.checksumHeaderBypass(checksumHeaderBypass());
        dbConfig.serializerRegistry(serializerRegistry());
        dbConfig.eventListeners(eventListeners());

        module.setStoreConfig(dbConfig);
        return module;
    }
}
