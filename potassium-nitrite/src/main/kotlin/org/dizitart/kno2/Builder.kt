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
import org.dizitart.no2.common.mapper.EntityConverter
import org.dizitart.no2.common.module.NitriteModule
import org.dizitart.no2.migration.Migration

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
     * The field separator used by the Nitrite database. By default, it is set to `.`.
     */
    var fieldSeparator: String = NitriteConfig.getFieldSeparator()

    /**
     * Enables/disables the repository type validation for the Nitrite database.
     * <p>
     * Repository type validation is a feature in Nitrite that ensures the type of the objects
     * stored in the repository can be converted to and from [org.dizitart.no2.collection.Document].
     * <p>
     * By default, the repository type validation is enabled. If you disable it, and if you try to
     * store an object that cannot be converted to a [org.dizitart.no2.collection.Document],
     * then Nitrite will throw an exception during the operation.
     *
     * @see org.dizitart.no2.collection.Document
     * @see org.dizitart.no2.repository.ObjectRepository
     * @see org.dizitart.no2.common.mapper.EntityConverter
     * @since 4.3.0
     */
    var validateRepositories = true

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

        if (!validateRepositories) {
            builder.disableRepositoryTypeValidation()
        }

        if (entityConverters.isNotEmpty()) {
            entityConverters.forEach { builder.registerEntityConverter(it) }
        }

        if (migrations.isNotEmpty()) {
            migrations.forEach { builder.addMigrations(it) }
        }

        modules.forEach { builder.loadModule(it) }

        builder.fieldSeparator(fieldSeparator)
        return builder
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
