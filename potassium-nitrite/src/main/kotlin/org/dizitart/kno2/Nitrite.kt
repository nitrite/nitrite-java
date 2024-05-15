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
import org.dizitart.no2.collection.NitriteCollection
import org.dizitart.no2.common.module.NitriteModule.module
import org.dizitart.no2.common.module.NitritePlugin
import org.dizitart.no2.common.tuples.Pair
import org.dizitart.no2.index.IndexOptions
import org.dizitart.no2.index.IndexType
import org.dizitart.no2.repository.EntityDecorator
import org.dizitart.no2.repository.ObjectRepository

/**
 * @since 2.1.0
 * @author Anindya Chatterjee
 */

/**
 * Opens a named collection from the store. If the collection does not exist it will be created
 * automatically and returned. If a collection is already opened, it is returned as is.
 *
 * Returned collection is thread-safe for concurrent use.
 *
 * The name cannot contain below reserved strings:
 *
 * - {@link Constants#INTERNAL_NAME_SEPARATOR}
 * - {@link Constants#USER_MAP}
 * - {@link Constants#INDEX_META_PREFIX}
 * - {@link Constants#INDEX_PREFIX}
 * - {@link Constants#OBJECT_STORE_NAME_SEPARATOR}
 *
 * @param [name] name of the collection
 * @param [op] collection builder block
 * @return the collection with the given name
 */
fun Nitrite.getCollection(
        name: String,
        op: (NitriteCollection.() -> Unit)? = null
): NitriteCollection {
    val collection = this.getCollection(name)
    op?.invoke(collection)
    return collection
}

/**
 * Opens a type-safe object repository from the store. If the repository does not exist it will be
 * created automatically and returned. If a repository is already opened, it is returned as is.
 *
 * The returned repository is thread-safe for concurrent use.
 *
 * @param [T] type parameter
 * @param [op] repository builder block
 * @return the repository of type [T]
 */
inline fun <reified T : Any> Nitrite.getRepository(
        noinline op: (ObjectRepository<T>.() -> Unit)? = null
): ObjectRepository<T> {
    val repository = this.getRepository(T::class.java)
    op?.invoke(repository)
    return repository
}

/**
 * Opens a type-safe object repository with a key identifier from the store. If the repository does
 * not exist it will be created automatically and returned. If a repository is already opened, it is
 * returned as is.
 *
 * The returned repository is thread-safe for concurrent use.
 *
 * @param [T] type parameter
 * @param key the key that will be appended to the repositories name
 * @param [op] repository builder block
 * @return the repository of type [T]
 */
inline fun <reified T : Any> Nitrite.getRepository(
        key: String,
        noinline op: (ObjectRepository<T>.() -> Unit)? = null
): ObjectRepository<T> {
    val repository = this.getRepository(T::class.java, key)
    op?.invoke(repository)
    return repository
}

/**
 * Opens a type-safe object repository using a {@link EntityDecorator}. If the
 * repository does not exist it will be created automatically and returned.
 * If a repository is already opened, it is returned as is.
 * <p>
 * The returned repository is thread-safe for concurrent use.
 *
 * @param [T] type parameter
 * @param [entityDecorator] the entityDecorator
 * @return the repository of type [T]
 */
inline fun <reified T : Any> Nitrite.getRepository(
        entityDecorator: EntityDecorator<T>,
        noinline op: (ObjectRepository<T>.() -> Unit)? = null
): ObjectRepository<T> {
    val repository = this.getRepository(entityDecorator)
    op?.invoke(repository)
    return repository
}

/**
 * Opens a type-safe object repository using a {@link EntityDecorator} and a key
 * identifier from the store. If the repository does not exist it will be
 * created
 * automatically and returned. If a repository is already opened, it is returned
 * as is.
 * <p>
 * The returned repository is thread-safe for concurrent use.
 *
 * @param [T] type parameter
 * @param [entityDecorator] the entityDecorator
 * @param [key]             the key
 * @return the repository of type [T]
 */
inline fun <reified T : Any> Nitrite.getRepository(
    entityDecorator: EntityDecorator<T>,
    key: String,
    noinline op: (ObjectRepository<T>.() -> Unit)? = null
): ObjectRepository<T> {
    val repository = this.getRepository(entityDecorator, key)
    op?.invoke(repository)
    return repository
}

/**
 * Returns an [IndexOptions] object with the specified index type.
 *
 * @param indexType the type of index to use, defaults to [IndexType.UNIQUE].
 * @return an [IndexOptions] object with the specified index type.
 */
fun option(indexType: String = IndexType.UNIQUE): IndexOptions =
        IndexOptions.indexOptions(indexType)

inline fun <reified T : Any> ObjectRepository<T>.insert(items: Iterable<T>) =
    insert(items.toList().toTypedArray())

fun Builder.loadModule(plugin: NitritePlugin) =
    loadModule(module(plugin))

operator fun <A, B> Pair<A, B>.component1(): A = first
operator fun <A, B> Pair<A, B>.component2(): B = second