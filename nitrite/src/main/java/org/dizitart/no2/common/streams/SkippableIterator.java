/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.common.streams;

import java.util.Iterator;

/**
 * An {@link Iterator} that can advance past records without materializing them.
 *
 * <p>Paging a collection is <code>find(filter, skipBy(n).limit(m))</code>, and a plain iterator can
 * only honour the <code>n</code> by calling {@link Iterator#next()} that many times. On a store
 * that decodes a document per <code>next()</code>, that is the whole cost of the request: the
 * deserialisation of every row the caller asked to be skipped, so page latency grows linearly with
 * the page number.
 *
 * <p>Every implementation of this interface is a storage-level iterator that can do better —
 * because it can advance a key cursor without reading values, or because the underlying tree can be
 * descended by index. A stream that cannot (one applying a filter, say — it has to look at each
 * document to know whether to keep it) simply does not implement this, and {@link BoundedStream}
 * falls back to the loop.
 *
 * @author Anindya Chatterjee
 * @since 5.1
 */
public interface SkippableIterator {

    /**
     * Advances this iterator past at most <code>count</code> records, as cheaply as the underlying
     * store allows, and returns how many it actually advanced.
     *
     * <p>A return value smaller than <code>count</code> means the iterator was exhausted, and is
     * the normal answer for a skip beyond the end of a collection rather than an error. Records
     * passed over this way are never returned by a subsequent {@link Iterator#next()}, exactly as
     * if <code>next()</code> had been called and the result discarded.
     *
     * @param count the number of records to advance past; never negative
     * @return the number of records actually advanced past
     */
    long skip(long count);
}
