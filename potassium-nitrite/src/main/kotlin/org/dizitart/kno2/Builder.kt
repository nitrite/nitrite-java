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

package org.dizitart.kno2

import com.fasterxml.jackson.databind.Module
import org.dizitart.no2.Nitrite
import org.dizitart.no2.NitriteBuilder
import org.dizitart.no2.index.TextIndexer
import org.dizitart.no2.index.fulltext.TextTokenizer
import org.dizitart.no2.mapper.NitriteMapper
import java.io.File

/**
 * A builder to create a nitrite database.
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
class Builder internal constructor() {
    private val jacksonModules = mutableSetOf<Module>()

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
     * Specifies a custom [TextIndexer] implementation to be used
     * during full text indexing and full text search. If not set, the default
     * text indexer will be used.
     */
    var textIndexer: TextIndexer? = null

    /**
     * Specifies a custom [TextTokenizer] for the in-built [TextIndexer].
     * If not set, a default text tokenizer [org.dizitart.no2.fulltext.EnglishTextTokenizer]
     * is used. The default tokenizer works on english language only.
     */
    var textTokenizer: TextTokenizer? = null

    /**
     * Specifies a custom [NitriteMapper] implementation. If not set, a default
     * jackson based mapper [KNO2JacksonMapper] will be used.
     */
    var nitriteMapper: NitriteMapper? = null

    /**
     * Disables JVM shutdown hook for closing the database gracefully.
     * */
    var disableShutdownHook: Boolean = false

    /**
     * Registers a jackson [Module] to the [KNO2JacksonFacade]
     *
     * @param [module] jackson [Module] to register
     * */
    fun registerModule(module: Module) {
        jacksonModules.add(module)
    }

    internal fun createNitriteBuilder() : NitriteBuilder {
        val builder = Nitrite.builder()
        if (file != null) {
            builder.filePath(file)
        } else {
            builder.filePath(path)
        }
        builder.autoCommitBufferSize(autoCommitBufferSize)
        builder.textIndexer(textIndexer)
        builder.textTokenizer(textTokenizer)

        if (nitriteMapper == null) {
            nitriteMapper = if (jacksonModules.isEmpty()) {
                KNO2JacksonMapper()
            } else {
                KNO2JacksonMapper(jacksonModules)
            }
        }
        builder.nitriteMapper(nitriteMapper)

        if (readOnly) builder.readOnly()
        if (compress) builder.compressed()
        if (!autoCommit) builder.disableAutoCommit()
        if (!autoCompact) builder.disableAutoCompact()
        if (disableShutdownHook) builder.disableShutdownHook()
        if (jacksonModules.isNotEmpty()) {
            jacksonModules.forEach { builder.registerModule(it) }
        }

        return builder
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
            op: (Builder.() -> Unit)? = null) : Nitrite {
    val builder = Builder()
    op?.invoke(builder)
    val nitriteBuilder = builder.createNitriteBuilder()
    return if (userId.isNullOrEmpty() && password.isNullOrEmpty()) {
        nitriteBuilder.openOrCreate()
    } else {
        nitriteBuilder.openOrCreate(userId, password)
    }
}