/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
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

package org.dizitart.no2.rocksdb;

import org.rocksdb.AbstractComparator;
import org.rocksdb.ComparatorOptions;
import org.rocksdb.util.BytewiseComparator;

import java.nio.ByteBuffer;


/**
 * @author Anindya Chatterjee
 */
public class NitriteRocksDBComparator extends AbstractComparator {
    private final Marshaller marshaller;
    private final AbstractComparator delegate;
    private final ComparatorOptions comparatorOptions;

    public NitriteRocksDBComparator(ComparatorOptions comparatorOptions, Marshaller marshaller) {
        super(comparatorOptions);
        this.marshaller = marshaller;
        this.delegate = new BytewiseComparator(comparatorOptions);
        this.comparatorOptions = comparatorOptions;
    }

    @Override
    public String name() {
        return "no2-rocksdb-comparator";
    }

    @Override
    public void findShortestSeparator(ByteBuffer start, ByteBuffer limit) {
        delegate.findShortestSeparator(start, limit);
    }

    @Override
    public void findShortSuccessor(ByteBuffer key) {
        delegate.findShortSuccessor(key);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" } )
    public int compare(ByteBuffer a, ByteBuffer b) {
        byte[] aBuffer = new byte[a.remaining()];
        a.get(aBuffer);

        byte[] bBuffer = new byte[b.remaining()];
        b.get(bBuffer);

        Comparable k1 = marshaller.unmarshal(aBuffer, Comparable.class);
        Comparable k2 = marshaller.unmarshal(bBuffer, Comparable.class);

        if (k1 == null && k2 == null) return 0;
        if (k1 == null) return 1;
        if (k2 == null) return -1;

        if (k1.getClass().equals(k2.getClass())) {
            return k1.compareTo(k2);
        } else {
            return delegate.compare(ByteBuffer.wrap(aBuffer), ByteBuffer.wrap(bBuffer));
        }
    }

    @Override
    public void close() {
        delegate.close();
        comparatorOptions.close();
        super.close();
    }
}
