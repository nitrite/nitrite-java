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

import org.dizitart.no2.Nitrite
import org.dizitart.no2.collection.*
import org.dizitart.no2.collection.objects.ObjectRepository

/**
 * @since 2.1.0
 * @author Anindya Chatterjee
 */

/**
 * Opens a named collection from the store. If the collections does not
 * exist it will be created automatically and returned. If a collection
 * is already opened, it is returned as is. Returned collection is thread-safe
 * for concurrent use.
 *
 * @param [name] name of the collection
 * @param [op] collection builder block
 * @return the collection
 */
fun Nitrite.getCollection(name: String, op: (NitriteCollection.() -> Unit)? = null): NitriteCollection {
    val collection = this.getCollection(name)
    op?.invoke(collection)
    return collection
}

/**
 * Opens a type-safe object repository from the store. If the repository
 * does not exist it will be created automatically and returned. If a
 * repository is already opened, it is returned as is.
 *
 * @param [T] type parameter
 * @param [op] repository builder block
 * @return the object repository of type [T]
 */
inline fun <reified T : Any> Nitrite.getRepository(noinline op: (ObjectRepository<T>.() -> Unit)? = null): ObjectRepository<T> {
    val repository = this.getRepository(T::class.java)
    op?.invoke(repository)
    return repository
}

/**
 * Opens a type-safe object repository with a key identifier from the store. If the repository
 * does not exist it will be created automatically and returned. If a
 * repository is already opened, it is returned as is.
 *
 * @param [T] type parameter
 * @param key  the key that will be appended to the repositories name
 * @param [op] repository builder block
 * @return the object repository of type [T]
 */
inline fun <reified T : Any> Nitrite.getRepository(key: String, noinline op: (ObjectRepository<T>.() -> Unit)? = null): ObjectRepository<T> {
    val repository = this.getRepository(key, T::class.java)
    op?.invoke(repository)
    return repository
}



/**
 * Creates an [IndexOptions] with the specified [indexType] and [async] flag.
 *
 * @param [indexType] the type of index to be created.
 * @param [async] if set to [true] then the index would be created asynchronously; otherwise synchronously.
 * @return a new [IndexOptions]
 */
fun option(indexType: IndexType = IndexType.Unique, async: Boolean = false) : IndexOptions
    = IndexOptions.indexOptions(indexType, async)

/**
 * Creates a [FindOptions] with pagination criteria.
 *
 * @param [offset] the pagination offset.
 * @param [size] the number of records per page.
 * @return a new [FindOptions]
 */
fun limit(offset: Int, size: Int): FindOptions
    = FindOptions.limit(offset, size)

/**
 * Creates a [FindOptions] with sorting criteria.
 *
 * @param [field] the value to sort by.
 * @param [sortOrder] the sort order.
 * @return a new [FindOptions]
 */
fun sort(field: String, sortOrder: SortOrder): FindOptions
    = FindOptions.sort(field, sortOrder)

/**
 * Creates a [FindOptions] with sorting criteria.
 *
 * @param [field] the value to sort by.
 * @param [sortOrder] the sort order.
 * @param [nullOrder] the `null` value order.
 * @return a new [FindOptions]
 */
fun sort(field: String, sortOrder: SortOrder, nullOrder: NullOrder): FindOptions
        = FindOptions.sort(field, sortOrder, nullOrder)
