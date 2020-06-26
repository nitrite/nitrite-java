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

import io.reactivex.Single;
import io.reactivex.internal.functions.ObjectHelper;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.WriteResult;

import java.util.concurrent.Callable;

/**
 * @author Anindya Chatterjee
 */
public final class FlowableWriteResult extends FlowableIterable<NitriteId> {
    private final Callable<WriteResult> supplier;

    FlowableWriteResult(Callable<WriteResult> supplier) {
        super(supplier);
        this.supplier = supplier;
    }

    Single<Integer> getAffectedCount() {
        return Single.fromCallable(() -> {
            WriteResult wrapped = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return wrapped.getAffectedCount();
        });
    }
}
