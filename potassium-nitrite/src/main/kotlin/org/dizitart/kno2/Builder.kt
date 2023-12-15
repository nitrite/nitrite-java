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
import org.dizitart.no2.common.Constants
import org.dizitart.no2.common.mapper.EntityConverter
import org.dizitart.no2.common.mapper.NitriteMapper
import org.dizitart.no2.common.module.NitriteModule
import org.dizitart.no2.common.module.NitriteModule.module
import org.dizitart.no2.migration.Migration
import org.dizitart.no2.spatial.SpatialIndexer

/**
 * A builder class for creating instances of [Nitrite].
 *
 * @since 2.1.0
 * @author Anindya Chatterjee
 */
class Builder internal constructor() {
    private val modules = mutableSetOf<NitriteModule>()
    private val entityConverters = mutableSetOf<EntityConverter<*>>()
    private val migrations = mutableSetOf<Migration>()

    /**
     * Sets the schema version for the Nitrite database.
     * */
    var schemaVersion: Int = 0

    /**
     * The field separator used by the Nitrite database. By default, it is set to the field
     * separator defined in the Nitrite configuration.
     */
    var fieldSeparator: String = NitriteConfig.getFieldSeparator()

    /**
     * Loads a [NitriteModule] into the Nitrite database. The module can be used to extend the
     * functionality of Nitrite.
     *
     * @param module the module to load.
     */
    fun loadModule(module: NitriteModule) {
        modules.add(module)
    }

    /**
     * Registers an [EntityConverter] with the Nitrite database.
     * An [EntityConverter] is used to convert between an entity and a
     * [org.dizitart.no2.collection.Document].
     * This method allows you to provide a custom converter for a specific class.
     *
     * @param converter the converter to register.
     */
    fun registerEntityConverter(converter: EntityConverter<*>) {
        entityConverters.add(converter)
    }

    /**
     * Registers a [Migration] with the Nitrite database.
     * A [Migration] is used to migrate the database from one version to another.
     *
     * @param migrations the migrations to register.
     */
    fun addMigration(vararg migrations: Migration) {
        this.migrations.addAll(migrations)
    }

    internal fun createNitriteBuilder(): NitriteBuilder {
        val builder = Nitrite.builder()

        if (schemaVersion > 0) {
            builder.schemaVersion(schemaVersion)
        }

        if (entityConverters.isNotEmpty()) {
            entityConverters.forEach { builder.registerEntityConverter(it) }
        }

        if (migrations.isNotEmpty()) {
            migrations.forEach { builder.addMigrations(it) }
        }

        modules.forEach { builder.loadModule(it) }
        loadDefaultPlugins(builder)

        builder.fieldSeparator(fieldSeparator)
        return builder
    }

    private fun loadDefaultPlugins(builder: NitriteBuilder) {
        val mapperFound =
            entityConverters.isNotEmpty() || modules.any { module -> module.plugins().any { it is NitriteMapper } }
        val spatialIndexerFound =
            modules.any { module -> module.plugins().any { it is SpatialIndexer } }

        if (!mapperFound && spatialIndexerFound) {
            builder.loadModule(module(KNO2JacksonMapper()))
        } else if (!spatialIndexerFound && mapperFound) {
            builder.loadModule(module(SpatialIndexer()))
        } else if (!mapperFound) {
            builder.loadModule(KNO2Module())
        }
    }
}

/**
 * Opens or creates a new Nitrite database. If it is configured as in-memory database, then it will
 * create a new database everytime. If it is configured as a file based database, and if the file
 * does not exist, then it will create a new file store and open the database; otherwise it will
 * open the existing database file.
 *
 * @param [userId] the user id
 * @param [password] the password
 * @return the nitrite database instance.
 */
fun nitrite(
    userId: String? = null,
    password: String? = null,
    op: (Builder.() -> Unit)? = null
): Nitrite {
    val builder = Builder()
    op?.invoke(builder)
    val nitriteBuilder = builder.createNitriteBuilder()
    return if (userId.isNullOrEmpty() && password.isNullOrEmpty()) {
        nitriteBuilder.openOrCreate()
    } else {
        nitriteBuilder.openOrCreate(userId, password)
    }
}
