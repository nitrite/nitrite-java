package org.dizitart.no2.collection.operation;

import org.dizitart.no2.common.RecordStream;

/**
 *
 * @author Anindya Chatterjee
 */
public interface StreamGenerator<T> {
    RecordStream<T> generate(FindOptions options);
}
