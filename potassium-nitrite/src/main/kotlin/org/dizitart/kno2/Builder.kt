/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.kno2

import org.dizitart.no2.Nitrite
import org.dizitart.no2.NitriteBuilder
import org.dizitart.no2.NitriteConfig
import org.dizitart.no2.module.NitriteModule
import org.dizitart.no2.module.NitriteModule.module
import org.dizitart.no2.spatial.SpatialIndexer
import org.dizitart.no2.store.events.StoreEventListener
import java.io.File

/**
 * A builder to create a nitrite database.
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
class Builder internal constructor() {
    private val modules = mutableSetOf<NitriteModule>()
    private val listeners = mutableSetOf<StoreEventListener>()

    /**
     * Path for the file based store.
     */
    var path: String? = null

    /**
     * [File] for the file based store.
     */
    var file: File? = null

    /**
     * The size of the write buffer, in KB disk space (for file-based
     * stores). Unless auto-commit is disabled, changes are automatically
     * saved if there are more than this amount of changes.
     *
     * When the values is set to 0 or lower, it will assume the default value
     * - 1024 KB.
     */
    var autoCommitBufferSize: Int = 0

    /**
     * Opens the file in read-only mode. In this case, a shared lock will be
     * acquired to ensure the file is not concurrently opened in write mode.
     *
     * If this option is not used, the file is locked exclusively.
     */
    var readOnly: Boolean = false

    /**
     * Compresses data before writing using the LZF algorithm. This will save
     * about 50% of the disk space, but will slow down read and write
     * operations slightly.
     */
    var compress: Boolean = false

    /**
     * Enables auto commit. If disabled, unsaved changes will not be written
     * into disk until {@link Nitrite#commit()} is called.
     */
    var autoCommit = true

    /**
     * Enables auto compact before close. If disabled, compaction will not
     * be performed. Disabling would increase close performance.
     */
    var autoCompact = true

    /**
     * Sets the thread pool shutdown timeout in seconds. Default value
     * is 5s.
     */
    var poolShutdownTimeout = 5

    /**
     * Enables off-heap storage for in-memory database.
     */
    var offHeapStorage = false;

    /**
     * Specifies the separator character for embedded field.
     * Default value is `.`
     *
     * */
    var fieldSeparator: String = NitriteConfig.getFieldSeparator()

    /**
     * Loads [NitriteModule] instances.
     * */
    fun loadModule(module: NitriteModule) {
        modules.add(module)
    }

    /**
     * Adds a [StoreEventListener] instance and subscribe it to store event.
     * */
    fun addStoreEventListener(listener: StoreEventListener) {
        listeners.add(listener)
    }

    internal fun createNitriteBuilder(): NitriteBuilder {
        val builder = NitriteBuilder.get()
        if (file != null) {
            builder.filePath(file)
        } else {
            builder.filePath(path)
        }
        builder.autoCommitBufferSize(autoCommitBufferSize)

        modules.forEach { builder.loadModule(it) }
        loadDefaultPlugins(builder)

        builder.fieldSeparator(fieldSeparator)

        if (readOnly) builder.readOnly()
        if (compress) builder.compressed()
        if (!autoCommit) builder.disableAutoCommit()
        if (!autoCompact) builder.disableAutoCompact()
        if (offHeapStorage) builder.enableOffHeapStorage()

        listeners.forEach { builder.addStoreEventListener(it) }

        return builder
    }

    private fun loadDefaultPlugins(builder: NitriteBuilder) {
        val mapperFound = modules.any { module -> module.plugins().any { it is KNO2JacksonMapper } }
        val spatialIndexerFound = modules.any { module -> module.plugins().any { it is SpatialIndexer } }

        if (!mapperFound && spatialIndexerFound) {
            builder.loadModule(module(KNO2JacksonMapper()))
        } else if (!spatialIndexerFound && mapperFound) {
            builder.loadModule(module(SpatialIndexer()))
        } else if (!mapperFound && !spatialIndexerFound) {
            builder.loadModule(KNO2Module())
        }
    }
}

/**
 * Opens or creates a new database. If it is an in-memory store, then it
 * will create a new one. If it is a file based store, and if the file does not
 * exists, then it will create a new file store and open; otherwise it will
 * open the existing file store.
 *
 * @param [userId] the user id
 * @param [password] the password
 * @return the nitrite database instance.
 */
fun nitrite(userId: String? = null, password: String? = null,
            op: (Builder.() -> Unit)? = null): Nitrite {
    val builder = Builder()
    op?.invoke(builder)
    val nitriteBuilder = builder.createNitriteBuilder()
    return if (userId.isNullOrEmpty() && password.isNullOrEmpty()) {
        nitriteBuilder.openOrCreate()
    } else {
        nitriteBuilder.openOrCreate(userId, password)
    }
}