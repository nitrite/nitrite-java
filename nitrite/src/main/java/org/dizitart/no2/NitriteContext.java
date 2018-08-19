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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.filters.ObjectFilters;
import org.dizitart.no2.index.TextIndexer;
import org.dizitart.no2.index.fulltext.TextTokenizer;
import org.dizitart.no2.mapper.JacksonMapper;
import org.dizitart.no2.mapper.NitriteMapper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static org.dizitart.no2.common.ExecutorServiceManager.shutdownExecutors;

/**
 * Represents a readonly view of all contextual information of a nitrite database.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 * @see Nitrite#getContext()
 */
@Slf4j
@Getter @Setter(AccessLevel.PACKAGE)
public class NitriteContext {
    /**
     * Gets the database file path. For in-memory database
     * it returns `null`.
     *
     * @returns database file path.
     * */
    private String filePath;

    /**
     * Gets the size of the auto-commit buffer. If the buffer size
     * exceeds this value and auto-commit is on, nitrite will save
     * the changes to disk.
     *
     * @returns auto-commit buffer size.
     *
     * */
    private int autoCommitBufferSize;

    /**
     * Indicates if this is an in-memory database or not.
     *
     * @returns `true`, if in-memory; otherwise `false`.
     * */
    private boolean inMemory;

    /**
     * Indicates if this is a readonly database or not.
     *
     * @returns `true`, if readonly; otherwise `false`.
     * */
    private boolean readOnly;

    /**
     * Indicates if this is a compressed database or not.
     *
     * @returns `true`, if compressed; otherwise `false`.
     * */
    private boolean compressed;

    /**
     * Indicates if auto commit is enabled or not.
     *
     * @returns `true`, if auto commit is enabled; otherwise `false`.
     * */
    private boolean autoCommitEnabled;

    /**
     * Indicates if auto compact is enabled or not before close.
     *
     * @returns `true`, if auto compact is enabled; otherwise `false`.
     * */
    private boolean autoCompactEnabled;

    /**
     * Gets the custom {@link TextIndexer} implementation used for the database.
     * It returns `null` if the default {@link TextIndexer} implementation
     * is used.
     *
     * @returns a {@link TextIndexer} instance or `null`.
     * @see org.dizitart.no2.filters.Filters#text(String, String)
     * @see ObjectFilters#text(String, String)
     * @see NitriteBuilder#textIndexer(TextIndexer)
     * */
    private TextIndexer textIndexer;

    /**
     * Gets the {@link TextTokenizer} implementation used for the database. It returns
     * `null` if the default {@link TextTokenizer} implementation is used.
     *
     * @returns a {@link TextTokenizer} instance or `null`.
     * @see org.dizitart.no2.filters.Filters#text(String, String)
     * @see ObjectFilters#text(String, String)
     * @see NitriteBuilder#textTokenizer(TextTokenizer)
     * */
    private TextTokenizer textTokenizer;

    @Getter(AccessLevel.PACKAGE)
    private Set<String> collectionRegistry;

    @Getter(AccessLevel.PACKAGE)
    private Map<String, Class<?>> repositoryRegistry;

    @Getter(AccessLevel.NONE)
    private NitriteMapper nitriteMapper;

    @Getter(AccessLevel.NONE)
    private Set<Module> jacksonModule;

    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private ExecutorService workerPool;

    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private ScheduledExecutorService scheduledWorkerPool;

    /**
     * Instantiates a new Nitrite context.
     */
    NitriteContext() {}

    /**
     * Gets the {@link NitriteMapper} instance configured.
     *
     * @return the {@link NitriteMapper}.
     */
    public NitriteMapper getNitriteMapper() {
        if (nitriteMapper == null) {
            if (jacksonModule == null || jacksonModule.isEmpty()) {
                nitriteMapper = new JacksonMapper();
            } else {
                nitriteMapper = new JacksonMapper(jacksonModule);
            }
        }
        return nitriteMapper;
    }

    /**
     * Gets the jackson {@link Module} registered with {@link JacksonMapper}.
     *
     * @return the set of jackson {@link Module}.
     */
    public Set<Module> getRegisteredModules() {
        return new HashSet<>(jacksonModule);
    }

    void shutdown() {
        shutdownExecutors(5);
        collectionRegistry.clear();
        repositoryRegistry.clear();
    }
}
