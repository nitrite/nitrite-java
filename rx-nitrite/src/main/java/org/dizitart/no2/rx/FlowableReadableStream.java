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

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.internal.functions.ObjectHelper;
import org.dizitart.no2.common.ReadableStream;

import java.util.concurrent.Callable;

/**
 * @author Anindya Chatterjee
 */
public abstract class FlowableReadableStream<T> extends FlowableIterable<T> {

    private final Callable<? extends ReadableStream<T>> supplier;

    FlowableReadableStream(Callable<? extends ReadableStream<T>> supplier) {
        super(supplier);
        this.supplier = supplier;
    }

    static <R> FlowableReadableStream<R> create(Callable<? extends ReadableStream<R>> supplier) {
        return new FlowableReadableStream<R>(supplier) {
        };
    }

    public Single<Long> size() {
        return Single.fromCallable(() -> {
            ReadableStream<T> recordIterable = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return recordIterable.size();
        });
    }

    public Maybe<T> firstOrNull() {
        return this.firstElement();
    }
}
