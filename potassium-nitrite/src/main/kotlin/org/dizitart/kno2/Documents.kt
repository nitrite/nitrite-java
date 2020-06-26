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

import org.dizitart.no2.collection.Document
import org.dizitart.no2.collection.Document.createDocument


/**
 * @since 2.1.0
 * @author Anindya Chatterjee
 */

/**
 * Creates an empty [Document].
 *
 * @return an empty [Document]
 */
fun emptyDocument(): Document = createDocument()

/**
 * Creates an empty [Document].
 *
 * @return an empty [Document]
 */
fun documentOf() = emptyDocument()

/**
 * Creates a [Document] from a [Pair].
 *
 * @return a [Document] containing the [pair]
 */
fun documentOf(pair: Pair<String, Any>): Document {
    return createDocument(pair.first, pair.second)!!
}

fun Document.isEmpty() = this.size() == 0

fun Document.isNotEmpty() = !this.isEmpty()

/**
 * Creates a [Document] from a list of [Pair]s.
 *
 * @return a [Document] containing the [pairs]
 */
fun documentOf(vararg pairs: Pair<String, Any>): Document {
    return if (pairs.isEmpty()) {
        emptyDocument()
    } else {
        val doc = emptyDocument()
        pairs.forEach { pair -> doc.put(pair.first, pair.second) }
        doc
    }
}
