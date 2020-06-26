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

import io.reactivex.Flowable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.operators.flowable.FlowableFromIterable;
import io.reactivex.internal.subscriptions.EmptySubscription;
import org.reactivestreams.Subscriber;

import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * @author Anindya Chatterjee
 */
public abstract class FlowableIterable<T> extends Flowable<T> {

    private final Callable<? extends Iterable<T>> supplier;

    FlowableIterable(Callable<? extends Iterable<T>> supplier) {
        this.supplier = supplier;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        Iterator<T> it;
        try {
            Iterable<T> iterable = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            it = iterable.iterator();
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptySubscription.error(e, s);
            return;
        }

        FlowableFromIterable.subscribe(s, it);
    }
}
