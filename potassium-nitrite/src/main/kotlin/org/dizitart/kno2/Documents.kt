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
 * Returns an empty [Document].
 *
 * @return the empty [Document].
 */
fun emptyDocument(): Document = createDocument()

/**
 * Returns an empty [Document].
 *
 * @return the empty [Document].
 */
fun documentOf() = emptyDocument()

/**
 * Creates a new [Document] instance with a single key-value pair.
 *
 * @param pair the key-value pair to add to the document.
 * @return the newly created document instance.
 */
fun documentOf(pair: Pair<String, Any?>): Document {
    return createDocument(pair.first, pair.second)!!
}

/**
 * Checks if the document is empty.
 *
 * @return `true` if the document is empty; `false` otherwise.
 */
fun Document.isEmpty() = this.size() == 0

/**
 * Checks if the document is not empty.
 *
 * @return `true` if the document is not empty; `false` otherwise.
 */
fun Document.isNotEmpty() = !this.isEmpty()

/**
 * Creates a new [Document] instance with the given key-value pairs.
 *
 * @param pairs the key-value pairs to be added to the document.
 * @return the newly created document.
 */
fun documentOf(vararg pairs: Pair<String, Any?>): Document {
    return if (pairs.isEmpty()) {
        emptyDocument()
    } else {
        val doc = emptyDocument()
        pairs.forEach { pair -> doc.put(pair.first, pair.second) }
        doc
    }
}
