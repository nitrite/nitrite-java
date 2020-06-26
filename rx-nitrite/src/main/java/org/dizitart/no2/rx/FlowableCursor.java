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

package org.dizitart.no2.rx;

import io.reactivex.internal.functions.ObjectHelper;
import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.NullOrder;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.repository.Cursor;

import java.text.Collator;
import java.util.concurrent.Callable;

/**
 * @author Anindya Chatterjee
 */
public final class FlowableCursor<T> extends FlowableReadableStream<T> {

    private final Callable<Cursor<T>> supplier;

    FlowableCursor(Callable<Cursor<T>> supplier) {
        super(supplier);
        this.supplier = supplier;
    }

    public FlowableCursor<T> sort(String field) {
        Callable<Cursor<T>> sortSupplier = () -> {
            Cursor<T> cursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return cursor.sort(field);
        };

        return new FlowableCursor<>(sortSupplier);
    }

    public FlowableCursor<T> sort(String field, SortOrder sortOrder) {
        Callable<Cursor<T>> sortSupplier = () -> {
            Cursor<T> cursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return cursor.sort(field, sortOrder);
        };

        return new FlowableCursor<>(sortSupplier);
    }

    public FlowableCursor<T> sort(String field, SortOrder sortOrder, Collator collator) {
        Callable<Cursor<T>> sortSupplier = () -> {
            Cursor<T> cursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return cursor.sort(field, sortOrder, collator);
        };

        return new FlowableCursor<>(sortSupplier);
    }

    public FlowableCursor<T> sort(String field, SortOrder sortOrder, Collator collator, NullOrder nullOrder) {
        Callable<Cursor<T>> sortSupplier = () -> {
            Cursor<T> cursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return cursor.sort(field, sortOrder, collator, nullOrder);
        };

        return new FlowableCursor<>(sortSupplier);
    }

    public FlowableCursor<T> limit(int offset, int size) {
        Callable<Cursor<T>> sortSupplier = () -> {
            Cursor<T> cursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return cursor.limit(offset, size);
        };

        return new FlowableCursor<>(sortSupplier);
    }

    public <P> FlowableReadableStream<P> project(Class<P> projectionType) {
        Callable<ReadableStream<P>> projectionSupplier = () -> {
            Cursor<T> cursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return cursor.project(projectionType);
        };

        return FlowableReadableStream.create(projectionSupplier);
    }

    public <Foreign, Joined> FlowableReadableStream<Joined> join(FlowableCursor<Foreign> foreignCursor, Lookup lookup,
                                                                 Class<Joined> type) {
        Callable<ReadableStream<Joined>> joinSupplier = () -> {
            Cursor<T> cursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");

            Cursor<Foreign> foreign = ObjectHelper.requireNonNull(foreignCursor.supplier.call(),
                "The supplier supplied is null");

            return cursor.join(foreign, lookup, type);
        };
        return FlowableReadableStream.create(joinSupplier);
    }
}
