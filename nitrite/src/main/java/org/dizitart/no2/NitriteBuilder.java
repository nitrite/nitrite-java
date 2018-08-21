/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2;

import com.fasterxml.jackson.databind.Module;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.SecurityException;
import org.dizitart.no2.index.TextIndexer;
import org.dizitart.no2.index.fulltext.EnglishTextTokenizer;
import org.dizitart.no2.index.fulltext.TextTokenizer;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteMVStore;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.util.StringUtils;
import org.h2.mvstore.MVStore;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.common.Constants.KEY_OBJ_SEPARATOR;
import static org.dizitart.no2.common.Security.createSecurely;
import static org.dizitart.no2.common.Security.openSecurely;
import static org.dizitart.no2.exceptions.ErrorCodes.NIOE_DIR_DOES_NOT_EXISTS;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.tool.Recovery.recover;
import static org.dizitart.no2.util.ObjectUtils.isKeyedObjectStore;
import static org.dizitart.no2.util.ObjectUtils.isObjectStore;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.util.ValidationUtils.isValidCollectionName;

/**
 * A builder utility to create a {@link Nitrite} database instance.
 *
 * === Example:
 *
 * [[app-listing]]
 * [source,java]
 * .Database with in-memory store
 * --
 *  Nitrite db = Nitrite.builder()
 *         .compressed()
 *         .openOrCreate("user", "password");
 * --
 *
 * [[app-listing]]
 * [source,java]
 * .Database with file store
 * --
 *  Nitrite db = Nitrite.builder()
 *         .filePath("/tmp/mydb.db")
 *         .openOrCreate();
 * --
 *
 * [[app-listing]]
 * [source,java]
 * .Database with user name and password
 * --
 *  Nitrite db = Nitrite.builder()
 *         .filePath("/tmp/mydb.db")
 *         .openOrCreate("user", "password");
 * --
 *
 * [[app-listing]]
 * [source,java]
 * .Database with custom {@link TextIndexer}
 * --
 *  Nitrite db = Nitrite.builder()
 *         .textIndexer(new LuceneService())
 *         .openOrCreate("user", "password");
 * --
 * [source,java]
 * --
 * include::src/test/java/org/dizitart/no2/services/LuceneService.java[]
 * --
 *
 * @author Anindya Chatterjee
 * @see Nitrite
 * @since 1.0
 */
@Slf4j
public class NitriteBuilder {
    private String filePath;
    private int autoCommitBufferSize;
    private boolean readOnly;
    private boolean compress;
    private boolean autoCommit = true;
    private boolean autoCompact = true;
    private boolean shutdownHook = true;
    private TextIndexer textIndexer;
    private TextTokenizer textTokenizer;
    private NitriteMapper nitriteMapper;
    private Set<Module> jacksonModules;

    NitriteBuilder(){
        jacksonModules = new HashSet<>();
    }

    /**
     * Sets file name for the file based store. If `file` is `null`
     * the builder will create an in-memory database.
     *
     * @param path the name of the file store.
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder filePath(String path) {
        this.filePath = path;
        return this;
    }

    /**
     * Sets file name for the file based store. If `file` is `null`
     * the builder will create an in-memory database.
     *
     * @param file the name of the file store.
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder filePath(File file) {
        if (file == null) {
            this.filePath = null;
        } else {
            this.filePath = file.getPath();
        }
        return this;
    }

    /**
     * Sets the size of the write buffer, in KB disk space (for file-based
     * stores). Unless auto-commit is disabled, changes are automatically
     * saved if there are more than this amount of changes.
     *
     * When the values is set to 0 or lower, it will assume the default value
     * - 1024 KB.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: If auto commit is disabled by {@link NitriteBuilder#disableAutoCommit()},
     * then buffer size has not effect.
     *
     * @param size the buffer size in KB
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder autoCommitBufferSize(int size) {
        this.autoCommitBufferSize = size;
        return this;
    }

    /**
     * Opens the file in read-only mode. In this case, a shared lock will be
     * acquired to ensure the file is not concurrently opened in write mode.
     *
     * If this option is not used, the file is locked exclusively.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: A file store may only be opened once in every JVM (no matter
     * whether it is opened in read-only or read-write mode), because each
     * file may be locked only once in a process.
     *
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder readOnly() {
        this.readOnly = true;
        return this;
    }

    /**
     * Compresses data before writing using the LZF algorithm. This will save
     * about 50% of the disk space, but will slow down read and write
     * operations slightly.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This setting only affects writes; it is not necessary to enable
     * compression when reading, even if compression was enabled when
     * writing.
     *
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder compressed() {
        this.compress = true;
        return this;
    }

    /**
     * Disables auto commit. If disabled, unsaved changes will not be written
     * into disk until {@link Nitrite#commit()} is called.
     *
     * By default auto commit is enabled.
     *
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder disableAutoCommit() {
        this.autoCommit = false;
        return this;
    }

    /**
     * Disables auto compact before close. If disabled, compaction will not
     * be performed. Disabling would increase close performance.
     *
     * By default auto compact is enabled.
     *
     * @return the {@link NitriteBuilder} instance.
     */
    public NitriteBuilder disableAutoCompact() {
        this.autoCompact = false;
        return this;
    }


    /**
     * Sets a custom {@link TextIndexer} implementation to be used
     * during full text indexing and full text search. If not set, the default
     * text indexer will be used.
     *
     * [icon="{@docRoot}/note.png"]
     * [NOTE]
     * --
     * If user does not want to use the default text indexer and instead like to use
     * third-party full text search engine like apache lucene, a custom
     * {@link TextIndexer} implementation needs to be provided here.
     *
     * --
     *
     * @param textIndexer the {@link TextIndexer} implementation.
     * @return the {@link NitriteBuilder} instance.
     * @see TextIndexer
     * @see org.dizitart.no2.filters.Filters#text(String, String)
     * @see NitriteCollection#createIndex(String, IndexOptions)
     */
    public NitriteBuilder textIndexer(TextIndexer textIndexer) {
        this.textIndexer = textIndexer;
        return this;
    }


    /**
     * Sets a custom {@link TextTokenizer} for the in-built {@link TextIndexer}.
     * If not set, a default text tokenizer {@link org.dizitart.no2.index.fulltext.EnglishTextTokenizer}
     * is used. The default tokenizer works on english language only.
     *
     * For non-english languages like chinese, japanese etc.,
     * a {@link org.dizitart.no2.index.fulltext.UniversalTextTokenizer} needs to be set here.
     *
     * [icon="{@docRoot}/alert.png"]
     * [CAUTION]
     * --
     * This settings is only applicable when in-built {@link TextIndexer} is
     * being used for full text indexing, in other words,
     * {@link NitriteBuilder#textIndexer(TextIndexer)} is not set.
     *
     * If a custom {@link TextIndexer} implementation is set, this settings has
     * no effect. The custom implementation has to take care of any necessary text
     * tokenizer.
     *
     * --
     * @param textTokenizer the {@link TextTokenizer} implementation.
     * @return the {@link NitriteBuilder} instance.
     * @see org.dizitart.no2.index.fulltext.EnglishTextTokenizer
     * @see org.dizitart.no2.index.fulltext.UniversalTextTokenizer
     */
    public NitriteBuilder textTokenizer(TextTokenizer textTokenizer) {
        this.textTokenizer = textTokenizer;
        return this;
    }

    /**
     * Sets a custom {@link NitriteMapper} implementation. If not set, a default
     * jackson based mapper {@link org.dizitart.no2.mapper.JacksonMapper} will
     * be used.
     *
     * @param nitriteMapper a {@link NitriteMapper} implementation
     * @return the {@link NitriteBuilder} instance.
     * @see org.dizitart.no2.mapper.GenericMapper
     * @see org.dizitart.no2.mapper.JacksonMapper
     * */
    public NitriteBuilder nitriteMapper(NitriteMapper nitriteMapper) {
        this.nitriteMapper = nitriteMapper;
        return this;
    }

    /**
     * Disables JVM shutdown hook for closing the database gracefully.
     *
     * @return the {@link NitriteBuilder} instance.
     * */
    public NitriteBuilder disableShutdownHook() {
        shutdownHook = false;
        return this;
    }

    /**
     * Registers a jackson {@link Module} to the {@link org.dizitart.no2.mapper.JacksonFacade}.
     *
     * [icon="{@docRoot}/note.png"]
     * [NOTE]
     * --
     * This is only useful when the default {@link NitriteMapper} viz.
     * {@link org.dizitart.no2.mapper.JacksonMapper} is used.
     *
     * --
     *
     * @param module jackson module to register
     * @return the {@link NitriteBuilder} instance.
     * @see org.dizitart.no2.mapper.JacksonFacade
     * @see org.dizitart.no2.mapper.JacksonMapper
     * @see NitriteMapper
     * @see org.dizitart.no2.mapper.GenericMapper
     * */
    public NitriteBuilder registerModule(Module module) {
        this.jacksonModules.add(module);
        return this;
    }

    /**
     * Opens or creates a new database. If it is an in-memory store, then it
     * will create a new one. If it is a file based store, and if the file does not
     * exists, then it will create a new file store and open; otherwise it will
     * open the existing file store.
     *
     * [icon="{@docRoot}/note.png"]
     * [NOTE]
     * --
     * If the database is corrupted somehow then at the time of opening, it will
     * try to repair it using the last known good version. If still it fails to
     * recover, then it will throw a {@link NitriteIOException}.
     *
     * It also adds a JVM shutdown hook to the database instance. If JVM exists
     * before closing the database properly by calling {@link Nitrite#close()},
     * then the shutdown hook will try to close the database as soon as possible
     * by discarding any unsaved changes to avoid database corruption.
     *
     * --
     *
     * @return the nitrite database instance.
     * @throws NitriteIOException if unable to create a new in-memory database.
     * @throws NitriteIOException if the database is corrupt and recovery fails.
     * @throws IllegalArgumentException if the directory does not exist.
     */
    public Nitrite openOrCreate() {
        return openOrCreateInternal(null, null);
    }

    /**
     * Opens or creates a new database. If it is an in-memory store, then it
     * will create a new one. If it is a file based store, and if the file does not
     * exists, then it will create a new file store and open; otherwise it will
     * open the existing file store.
     *
     * While creating a new database, it will use the specified user credentials.
     * While opening an existing database, it will use the specified credentials
     * to open it.
     *
     * [icon="{@docRoot}/note.png"]
     * [NOTE]
     * --
     * If the database is corrupted somehow then at the time of opening, it will
     * try to repair it using the last known good version. If still it fails to
     * recover, then it will throw a {@link NitriteIOException}.
     *
     * It also adds a JVM shutdown hook to the database instance. If JVM exists
     * before closing the database properly by calling {@link Nitrite#close()},
     * then the shutdown hook will try to close the database as soon as possible
     * by discarding any unsaved changes to avoid database corruption.
     *
     * --
     *
     * @param userId   the user id
     * @param password the password
     * @return the nitrite database instance.
     * @throws SecurityException if the user credentials are wrong or one of them is empty string.
     * @throws NitriteIOException if unable to create a new in-memory database.
     * @throws NitriteIOException if the database is corrupt and recovery fails.
     * @throws NitriteIOException if the directory does not exist.
     */
    public Nitrite openOrCreate(String userId, String password) {
        if (StringUtils.isNullOrEmpty(userId)) {
            throw new SecurityException(USER_ID_IS_EMPTY);
        }
        if (StringUtils.isNullOrEmpty(password)) {
            throw new SecurityException(PASSWORD_IS_EMPTY);
        }
        return openOrCreateInternal(userId, password);
    }

    private Nitrite openOrCreateInternal(String userId, String password) {
        MVStore.Builder builder = new MVStore.Builder();

        if (!isNullOrEmpty(filePath)) {
            builder = builder.fileName(filePath);
        }

        if (autoCommitBufferSize > 0) {
            builder = builder.autoCommitBufferSize(autoCommitBufferSize);
        }

        if (readOnly) {
            if (isNullOrEmpty(filePath)) {
                throw new InvalidOperationException(UNABLE_TO_CREATE_IN_MEMORY_READONLY_DB);
            }
            builder = builder.readOnly();
        }

        if (compress) {
            builder = builder.compress();
        }

        if (!autoCommit) {
            builder = builder.autoCommitDisabled();
        }

        // auto compact disabled github issue #41
        builder.autoCompactFillRate(0);

        MVStore store = null;
        File dbFile = null;
        try {
            if (!isNullOrEmpty(filePath)) {
                dbFile = new File(filePath);
                if (dbFile.exists()) {
                    store = openSecurely(builder, userId, password);
                } else {
                    store = createSecurely(builder, userId, password);
                }
            } else {
                store = createSecurely(builder, userId, password);
            }
        } catch (IllegalStateException ise) {
            if (ise.getMessage().contains("file is locked")) {
                throw new NitriteIOException(DATABASE_OPENED_IN_OTHER_PROCESS);
            }

            if (!isNullOrEmpty(filePath)) {
                try {
                    File file = new File(filePath);
                    if (file.exists() && file.isFile()) {
                        log.error("Database corruption detected. Trying to repair", ise);
                        recover(filePath);
                        store = builder.open();
                    } else {
                        if (readOnly) {
                            throw new NitriteIOException(FAILED_TO_CREATE_IN_MEMORY_READONLY_DB, ise);
                        }
                    }
                } catch (InvalidOperationException ioe) {
                    throw ioe;
                } catch (Exception e) {
                    throw new NitriteIOException(UNABLE_TO_REPAIR_DB, e);
                }
            } else {
                throw new NitriteIOException(UNABLE_TO_CREATE_IN_MEMORY_DB, ise);
            }
        } catch (IllegalArgumentException iae) {
            if (dbFile != null) {
                if (!dbFile.getParentFile().exists()) {
                    throw new NitriteIOException(errorMessage("Directory "+ dbFile.getParent() + " does not exists",
                            NIOE_DIR_DOES_NOT_EXISTS), iae);
                }
            }
            throw new NitriteIOException(UNABLE_TO_CREATE_DB_FILE, iae);
        } finally {
            if (store != null) {
                store.setRetentionTime(-1);
                store.setVersionsToKeep(2);
                store.setReuseSpace(true);
            }
        }

        if (store != null) {
            NitriteContext context = new NitriteContext();
            context.setTextIndexer(textIndexer);
            if (textTokenizer == null) {
                textTokenizer = new EnglishTextTokenizer();
            }
            context.setTextTokenizer(textTokenizer);
            context.setFilePath(filePath);
            if (autoCommitBufferSize > 0) {
                context.setAutoCommitBufferSize(autoCommitBufferSize);
            } else {
                context.setAutoCommitBufferSize(1024);
            }
            context.setInMemory(isNullOrEmpty(filePath));
            context.setReadOnly(readOnly);
            context.setCompressed(compress);
            context.setAutoCommitEnabled(autoCommit);
            context.setAutoCompactEnabled(autoCompact);
            context.setNitriteMapper(nitriteMapper);
            context.setJacksonModule(jacksonModules);

            NitriteStore nitriteStore = new NitriteMVStore(store);

            // populate existing maps
            context.setCollectionRegistry(populateCollections(nitriteStore));
            context.setRepositoryRegistry(populateRepositories(nitriteStore));

            Nitrite db = new Nitrite(nitriteStore, context);

            // shutdown hook to close db file gracefully
            if (shutdownHook) {
                Runtime.getRuntime().addShutdownHook(new NitriteShutDownHook(db));
            }
            return db;
        }
        return null;
    }

    private Set<String> populateCollections(NitriteStore store) {
        Set<String> collectionRegistry = new HashSet<>();
        if (store != null) {
            for (String name : store.getMapNames()) {
                if (isValidCollectionName(name) && !isObjectStore(name)) {
                    collectionRegistry.add(name);
                }
            }
        } else {
            log.error("Underlying store is null. Nitrite has not been initialized properly.");
        }
        return collectionRegistry;
    }

    private Map<String, Class<?>> populateRepositories(NitriteStore store) {
        Map<String, Class<?>> repositoryRegistry = new HashMap<>();
        if (store != null) {
            for (String name : store.getMapNames()) {
                if (isValidCollectionName(name) && isObjectStore(name)) {
                    try {
                        if (isKeyedObjectStore(name)) {
                            String[] split = name.split("\\" + KEY_OBJ_SEPARATOR);
                            String typeName = split[0];
                            Class<?> type = Class.forName(typeName);
                            repositoryRegistry.put(name, type);
                        } else {
                            Class<?> type = Class.forName(name);
                            repositoryRegistry.put(name, type);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error("Could not find the class " + name);
                    }
                }
            }
        } else {
            log.error("Underlying store is null. Nitrite has not been initialized properly.");
        }
        return repositoryRegistry;
    }

    private static class NitriteShutDownHook extends Thread {
        private Nitrite db;

        NitriteShutDownHook(Nitrite db) {
            this.db = db;
        }

        @Override
        public void run() {
            if (db != null && !db.isClosed()) {
                try {
                    db.close();
                } catch (Throwable t) {
                    // close the db immediately and discards
                    // any unsaved changes to avoid corruption
                    log.error("Error while database shutdown", t);
                    db.closeImmediately();
                }
            }
        }
    }
}
